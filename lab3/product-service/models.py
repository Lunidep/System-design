from sqlalchemy import Column, String, Text, Numeric
from database import Base

class Product(Base):
    __tablename__ = "products"

    id = Column(String(50), primary_key=True, index=True)
    name = Column(String(100), nullable=False, index=True)
    description = Column(Text)
    price = Column(Numeric(10, 2), nullable=False)
    category = Column(String(50), index=True)

    def __repr__(self):
        return f"<Product(id={self.id}, name={self.name}, price={self.price})>"