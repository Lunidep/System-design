from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from typing import List, Optional
import asyncpg
from datetime import datetime, timedelta
import jwt
from passlib.context import CryptContext
from uvicorn import run
from redis import asyncio as aioredis
import logging
import json

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
REDIS_URL = "redis://redis:6379"
SECRET_KEY = "my-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

async def get_redis():
    redis = await aioredis.from_url(REDIS_URL)
    try:
        yield redis
    finally:
        await redis.close()

async def get_db():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        yield conn
    finally:
        await conn.close()

async def get_cached_user(username: str, redis=Depends(get_redis)):
    cached = await redis.get(f"user:{username}")
    if cached:
        return json.loads(cached)
    return None

async def cache_user(user: dict, redis=Depends(get_redis)):
    await redis.setex(
        f"user:{user['username']}",
        timedelta(minutes=5),
        json.dumps(user)
    )

@app.get("/users/{username}", response_model=User)
async def read_user(
    username: str,
    db=Depends(get_db),
    redis=Depends(get_redis),
    current_user: User = Depends(get_current_user)
):
    cached_user = await get_cached_user(username, redis)
    if cached_user:
        logger.info(f"Returning cached user: {username}")
        return cached_user

    user = await db.fetchrow(
        "SELECT * FROM users WHERE username = $1", username
    )
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_dict = dict(user)
    await cache_user(user_dict, redis)  # кеш на 5 минут
    return user_dict

@app.get("/users/search/", response_model=List[User])
async def search_users(
    name: str,
    db=Depends(get_db),
    redis=Depends(get_redis)
):
    cache_key = f"user_search:{name.lower()}"
    cached = await redis.get(cache_key)
    if cached:
        return json.loads(cached)

    users = await db.fetch(
        "SELECT * FROM users WHERE full_name ILIKE $1", f"%{name}%"
    )
    result = [dict(u) for u in users]
    await redis.setex(cache_key, 60, json.dumps(result))  # кеш на 1 минуту
    return result