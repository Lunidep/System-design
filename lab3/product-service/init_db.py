from sqlalchemy import create_engine
from models import Base
import os

def init_db():
    DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://admin:secret@localhost:5432/shop_db")
    engine = create_engine(DATABASE_URL)
    Base.metadata.create_all(bind=engine)
    print("Database tables created successfully")

if __name__ == "__main__":
    init_db()