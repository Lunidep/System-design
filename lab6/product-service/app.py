import logging
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel
from typing import List, Optional
from motor.motor_asyncio import AsyncIOMotorClient
from datetime import datetime, timedelta
import jwt
from passlib.context import CryptContext
from uvicorn import run
from confluent_kafka import Producer
import json

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("product_service.log")
    ]
)
logger = logging.getLogger(__name__)

app = FastAPI()

MONGO_URL = "mongodb://mongo:27017"
DATABASE_NAME = "shop_db"
KAFKA_BROKER = "kafka:9092"
KAFKA_TOPIC = "products"

SECRET_KEY = "my-secret-key"
ALGORITHM = "HS256"
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="http://user-service:8000/token")


class Product(BaseModel):
    id: str
    name: str
    description: Optional[str] = None
    price: float
    category: Optional[str] = None


class ProductCreate(BaseModel):
    name: str
    description: Optional[str] = None
    price: float
    category: Optional[str] = None


async def get_db():
    client = AsyncIOMotorClient(MONGO_URL)
    db = client[DATABASE_NAME]
    try:
        yield db
    finally:
        client.close()


def get_kafka_producer():
    conf = {'bootstrap.servers': KAFKA_BROKER}
    return Producer(**conf)


@app.on_event("startup")
async def startup_event():
    logger.info("Initializing MongoDB...")
    client = AsyncIOMotorClient(MONGO_URL)
    db = client[DATABASE_NAME]
    try:
        await db.products.create_index([("product_id", 1)], unique=True)
        await db.products.create_index([("category", 1)])
        logger.info("MongoDB indexes created")

        if await db.products.count_documents({}) == 0:
            await db.products.insert_many([
                {
                    "product_id": "prod_1",
                    "name": "Laptop",
                    "description": "High performance laptop",
                    "price": 999.99,
                    "category": "Electronics",
                    "created_at": datetime.utcnow()
                },
                {
                    "product_id": "prod_2",
                    "name": "Smartphone",
                    "description": "Latest model",
                    "price": 699.99,
                    "category": "Electronics",
                    "created_at": datetime.utcnow()
                }
            ])
            logger.info("Sample products added to MongoDB")
    except Exception as e:
        logger.error(f"MongoDB initialization error: {e}")


async def verify_token(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid credentials",
            )
        logger.info(f"Authenticated user: {username}")
        return username
    except jwt.ExpiredSignatureError:
        logger.warning("Token expired")
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        logger.warning("Invalid token")
        raise HTTPException(status_code=401, detail="Invalid token")


def delivery_report(err, msg):
    if err is not None:
        logger.error(f'Message delivery failed: {err}')
    else:
        logger.info(f'Message delivered to {msg.topic()} [{msg.partition()}]')


@app.post("/products/", response_model=Product, status_code=201)
async def create_product(
        product: ProductCreate,
        db=Depends(get_db),
        username: str = Depends(verify_token)
):
    """Создание продукта - отправка в Kafka"""
    product_id = f"prod_{datetime.now().timestamp()}"
    product_data = {
        "product_id": product_id,
        **product.dict(),
        "created_at": datetime.utcnow().isoformat(),
        "action": "create"
    }

    producer = get_kafka_producer()
    try:
        producer.produce(
            topic=KAFKA_TOPIC,
            value=json.dumps(product_data),
            callback=delivery_report
        )
        producer.flush()
        logger.info(f"Product event sent to Kafka: {product_id}")
    except Exception as e:
        logger.error(f"Error sending to Kafka: {e}")
        raise HTTPException(status_code=500, detail="Error processing product")

    return product_data


@app.get("/products/", response_model=List[Product])
async def get_products(
        category: Optional[str] = None,
        skip: int = 0,
        limit: int = 10,
        db=Depends(get_db)
):
    """Получение списка продуктов из MongoDB"""
    query = {"category": category} if category else {}
    products = await db.products.find(query).skip(skip).limit(limit).to_list(limit)
    return products


@app.get("/products/{product_id}", response_model=Product)
async def get_product(product_id: str, db=Depends(get_db)):
    """Получение продукта по ID из MongoDB"""
    product = await db.products.find_one({"product_id": product_id})
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product


@app.put("/products/{product_id}", response_model=Product)
async def update_product(
        product_id: str,
        product: ProductCreate,
        db=Depends(get_db),
        username: str = Depends(verify_token)
):
    """Обновление продукта - отправка в Kafka"""
    product_data = {
        "product_id": product_id,
        **product.dict(),
        "updated_at": datetime.utcnow().isoformat(),
        "action": "update"
    }

    producer = get_kafka_producer()
    try:
        producer.produce(
            topic=KAFKA_TOPIC,
            value=json.dumps(product_data),
            callback=delivery_report
        )
        producer.flush()
        logger.info(f"Product update event sent to Kafka: {product_id}")
    except Exception as e:
        logger.error(f"Error sending to Kafka: {e}")
        raise HTTPException(status_code=500, detail="Error processing product update")

    return product_data


@app.delete("/products/{product_id}", status_code=204)
async def delete_product(
        product_id: str,
        db=Depends(get_db),
        username: str = Depends(verify_token)
):
    """Удаление продукта - отправка в Kafka"""
    delete_data = {
        "product_id": product_id,
        "action": "delete",
        "deleted_at": datetime.utcnow().isoformat()
    }

    producer = get_kafka_producer()
    try:
        producer.produce(
            topic=KAFKA_TOPIC,
            value=json.dumps(delete_data),
            callback=delivery_report
        )
        producer.flush()
        logger.info(f"Product delete event sent to Kafka: {product_id}")
    except Exception as e:
        logger.error(f"Error sending to Kafka: {e}")
        raise HTTPException(status_code=500, detail="Error processing product deletion")


if __name__ == "__main__":
    run(app, host="0.0.0.0", port=8001)