import logging
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from typing import List, Optional
import asyncpg
from datetime import datetime, timedelta
import jwt
from passlib.context import CryptContext
from uvicorn import run

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("user_service.log")
    ]
)
logger = logging.getLogger(__name__)

app = FastAPI()

DATABASE_URL = "postgresql://postgres:postgres@database:5432/shop_db"

SECRET_KEY = "my-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

class User(BaseModel):
    username: str
    full_name: Optional[str] = None
    email: Optional[str] = None
    disabled: Optional[bool] = None

class UserCreate(BaseModel):
    username: str
    full_name: str
    email: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str

class CartItem(BaseModel):
    product_id: str
    quantity: int

class Cart(BaseModel):
    user_id: str
    items: List[CartItem]

async def get_db():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        yield conn
    finally:
        await conn.close()

@app.on_event("startup")
async def startup_event():
    logger.info("Initializing database...")
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        await conn.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                full_name VARCHAR(100),
                email VARCHAR(100),
                hashed_password VARCHAR(100) NOT NULL,
                disabled BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        await conn.execute('''
            CREATE TABLE IF NOT EXISTS carts (
                user_id VARCHAR(50) PRIMARY KEY,
                items JSONB DEFAULT '[]'::jsonb,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        count = await conn.fetchval("SELECT COUNT(*) FROM users")
        if count == 0:
            hashed_pass = pwd_context.hash("secret")
            await conn.execute('''
                INSERT INTO users (username, full_name, email, hashed_password)
                VALUES ($1, $2, $3, $4)
            ''', "admin", "Admin User", "admin@example.com", hashed_pass)
            
            logger.info("Test user 'admin' created")
    except Exception as e:
        logger.error(f"Database initialization error: {e}")
    finally:
        await conn.close()

async def get_user(db, username: str):
    user = await db.fetchrow(
        "SELECT * FROM users WHERE username = $1", username
    )
    if user:
        return dict(user)

async def authenticate_user(username: str, password: str, db):
    user = await get_user(db, username)
    if not user:
        return False
    if not pwd_context.verify(password, user["hashed_password"]):
        return False
    return user

def create_access_token(data: dict, expires_delta: timedelta):
    to_encode = data.copy()
    expire = datetime.utcnow() + expires_delta
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db = Depends(get_db)
):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except jwt.ExpiredSignatureError:
        logger.warning("Token expired")
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        logger.warning("Invalid token")
        raise HTTPException(status_code=401, detail="Invalid token")
    
    user = await get_user(db, username)
    if user is None:
        raise credentials_exception
    return user

@app.post("/token", response_model=Token)
async def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db = Depends(get_db)
):
    user = await authenticate_user(form_data.username, form_data.password, db)
    if not user:
        logger.warning(f"Failed login attempt for user: {form_data.username}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
        )
    
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user["username"]}, expires_delta=access_token_expires
    )
    logger.info(f"User {user['username']} logged in successfully")
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/users/", response_model=User, status_code=201)
async def create_user(
    user: UserCreate,
    db = Depends(get_db)
):
    logger.info(f"Creating user: {user.username}")
    existing_user = await get_user(db, user.username)
    if existing_user:
        logger.warning(f"Username {user.username} already exists")
        raise HTTPException(status_code=400, detail="Username already registered")
    
    hashed_password = pwd_context.hash(user.password)
    try:
        await db.execute('''
            INSERT INTO users (username, full_name, email, hashed_password)
            VALUES ($1, $2, $3, $4)
        ''', user.username, user.full_name, user.email, hashed_password)
        
        await db.execute('''
            INSERT INTO carts (user_id)
            VALUES ($1)
        ''', user.username)
        
        logger.info(f"User {user.username} created successfully")
        return {
            "username": user.username,
            "full_name": user.full_name,
            "email": user.email,
            "disabled": False
        }
    except Exception as e:
        logger.error(f"Error creating user: {e}")
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/users/me", response_model=User)
async def read_users_me(current_user: User = Depends(get_current_user)):
    return current_user

@app.get("/users/{username}", response_model=User)
async def read_user(
    username: str,
    db = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    logger.info(f"Fetching user {username} by {current_user['username']}")
    user = await get_user(db, username)
    if not user:
        logger.warning(f"User {username} not found")
        raise HTTPException(status_code=404, detail="User not found")
    return user

@app.get("/users/search/", response_model=List[User])
async def search_users(
    name: Optional[str] = None,
    db = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if not name:
        return []
    
    logger.info(f"Searching users by name: {name}")
    users = await db.fetch(
        "SELECT * FROM users WHERE full_name ILIKE $1", f"%{name}%"
    )
    return [dict(u) for u in users]

@app.post("/cart/add", response_model=Cart)
async def add_to_cart(
    product_id: str,
    quantity: int = 1,
    db = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    logger.info(f"Adding product {product_id} to cart for user {current_user['username']}")
    
    cart = await db.fetchrow(
        "SELECT items FROM carts WHERE user_id = $1", current_user["username"]
    )
    
    if not cart:
        items = [{"product_id": product_id, "quantity": quantity}]
        await db.execute('''
            INSERT INTO carts (user_id, items)
            VALUES ($1, $2::jsonb)
        ''', current_user["username"], items)
    else:
        items = cart["items"]
        found = False
        for item in items:
            if item["product_id"] == product_id:
                item["quantity"] += quantity
                found = True
                break
        
        if not found:
            items.append({"product_id": product_id, "quantity": quantity})
        
        await db.execute('''
            UPDATE carts SET items = $1::jsonb, updated_at = NOW()
            WHERE user_id = $2
        ''', items, current_user["username"])
    
    return {
        "user_id": current_user["username"],
        "items": items
    }

@app.get("/cart", response_model=Cart)
async def get_cart(
    db = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    cart = await db.fetchrow(
        "SELECT * FROM carts WHERE user_id = $1", current_user["username"]
    )
    if not cart:
        return {"user_id": current_user["username"], "items": []}
    return dict(cart)

if __name__ == "__main__":
    run(app, host="0.0.0.0", port=8000)