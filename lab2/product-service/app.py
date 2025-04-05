from fastapi import FastAPI, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

products_db = []

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

@app.post("/products/", response_model=Product)
async def create_product(
    product: ProductCreate,
    token: str = Depends(oauth2_scheme)
):
    product_id = f"prod_{len(products_db) + 1}"
    new_product = Product(id=product_id, **product.dict())
    products_db.append(new_product.dict())
    return new_product

@app.get("/products/", response_model=List[Product])
async def get_products(
    category: Optional[str] = None,
    skip: int = 0,
    limit: int = 10
):
    if category:
        return [p for p in products_db if p["category"] == category][skip:skip + limit]
    return products_db[skip:skip + limit]

@app.get("/products/{product_id}", response_model=Product)
async def get_product(product_id: str):
    for product in products_db:
        if product["id"] == product_id:
            return product
    raise HTTPException(status_code=404, detail="Product not found")