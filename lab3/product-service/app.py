from fastapi import FastAPI, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel
from typing import List, Optional
from sqlalchemy.orm import Session
from database import get_db
from models import Product

app = FastAPI()

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


class ProductBase(BaseModel):
    name: str
    description: Optional[str] = None
    price: float
    category: Optional[str] = None


class ProductCreate(ProductBase):
    pass


class Product(ProductBase):
    id: str

    class Config:
        orm_mode = True


@app.on_event("startup")
async def startup():
    # Initialize database tables
    from init_db import init_db
    init_db()


@app.post("/products/", response_model=Product)
async def create_product(
        product: ProductCreate,
        token: str = Depends(oauth2_scheme),
        db: Session = Depends(get_db)
):
    product_id = f"prod_{db.query(Product).count() + 1}"
    db_product = Product(
        id=product_id,
        name=product.name,
        description=product.description,
        price=product.price,
        category=product.category
    )
    db.add(db_product)
    db.commit()
    db.refresh(db_product)
    return db_product


@app.get("/products/", response_model=List[Product])
async def get_products(
        category: Optional[str] = None,
        skip: int = 0,
        limit: int = 10,
        db: Session = Depends(get_db)
):
    query = db.query(Product)
    if category:
        query = query.filter(Product.category == category)
    return query.offset(skip).limit(limit).all()


@app.get("/products/{product_id}", response_model=Product)
async def get_product(
        product_id: str,
        db: Session = Depends(get_db)
):
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product


@app.put("/products/{product_id}", response_model=Product)
async def update_product(
        product_id: str,
        product: ProductCreate,
        token: str = Depends(oauth2_scheme),
        db: Session = Depends(get_db)
):
    db_product = db.query(Product).filter(Product.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="Product not found")

    db_product.name = product.name
    db_product.description = product.description
    db_product.price = product.price
    db_product.category = product.category

    db.commit()
    db.refresh(db_product)
    return db_product


@app.delete("/products/{product_id}")
async def delete_product(
        product_id: str,
        token: str = Depends(oauth2_scheme),
        db: Session = Depends(get_db)
):
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")

    db.delete(product)
    db.commit()
    return {"message": "Product deleted successfully"}