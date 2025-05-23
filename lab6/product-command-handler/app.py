import logging
from motor.motor_asyncio import AsyncIOMotorClient
from confluent_kafka import Consumer, KafkaError, KafkaException
import json
import asyncio
import socket

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("command_handler.log")
    ]
)
logger = logging.getLogger(__name__)

MONGO_URL = "mongodb://mongo:27017"
DATABASE_NAME = "shop_db"
KAFKA_BROKER = "kafka:9092"
KAFKA_TOPIC = "products"


def create_kafka_consumer():
    return Consumer({
        'bootstrap.servers': KAFKA_BROKER,
        'group.id': 'product_command_handler',
        'auto.offset.reset': 'earliest',
        'enable.auto.commit': False,
        'session.timeout.ms': 10000,
        'heartbeat.interval.ms': 3000
    })


async def wait_for_kafka():
    """Ожидание доступности Kafka и топика"""
    max_retries = 30
    for i in range(max_retries):
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.settimeout(2)
                s.connect(('kafka', 9092))

            consumer = create_kafka_consumer()

            metadata = consumer.list_topics(timeout=10)

            if KAFKA_TOPIC not in metadata.topics:
                from confluent_kafka.admin import AdminClient, NewTopic
                admin = AdminClient({'bootstrap.servers': KAFKA_BROKER})
                new_topic = NewTopic(KAFKA_TOPIC, num_partitions=1, replication_factor=1)
                admin.create_topics([new_topic])
                await asyncio.sleep(2)

            consumer.subscribe([KAFKA_TOPIC])
            consumer.poll(1.0)
            assignment = consumer.assignment()
            if not assignment:
                raise Exception(f"Failed to subscribe to topic {KAFKA_TOPIC}")

            consumer.close()
            return True

        except Exception as e:
            if i < max_retries - 1:
                logger.info(f"Waiting for Kafka... attempt {i + 1}/{max_retries}. Error: {str(e)}")
                await asyncio.sleep(5)
            else:
                logger.error(f"Kafka is not available after multiple retries. Last error: {str(e)}")
                raise


async def process_message(db, consumer, msg):
    """Обработка одного сообщения из Kafka"""
    try:
        product_data = json.loads(msg.value().decode('utf-8'))
        action = product_data.get("action")

        if action == "create":
            await db.products.insert_one(product_data)
            logger.info(f"Created product: {product_data['product_id']}")

        elif action == "update":
            product_id = product_data["product_id"]
            update_data = {k: v for k, v in product_data.items()
                           if k not in ["product_id", "action"]}
            result = await db.products.update_one(
                {"product_id": product_id},
                {"$set": update_data}
            )
            if result.modified_count == 0:
                logger.warning(f"Product not found: {product_id}")

        elif action == "delete":
            result = await db.products.delete_one({"product_id": product_data["product_id"]})
            if result.deleted_count == 0:
                logger.warning(f"Product not found: {product_data['product_id']}")

        consumer.commit(message=msg, asynchronous=False)
    except json.JSONDecodeError as e:
        logger.error(f"Invalid message format: {e}")
    except Exception as e:
        logger.error(f"Error processing message: {e}")


async def handle_product_commands():
    """Основной цикл обработки команд"""
    global msg
    await wait_for_kafka()

    mongo_client = AsyncIOMotorClient(MONGO_URL)
    db = mongo_client[DATABASE_NAME]

    consumer = create_kafka_consumer()
    consumer.subscribe([KAFKA_TOPIC])

    logger.info("Product Command Handler started successfully")

    try:
        while True:
            msg = consumer.poll(1.0)

            if msg is None:
                continue

            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    logger.debug("Reached end of partition")
                    continue
                logger.error(f"Kafka error: {msg.error()}")
                continue

            await process_message(db, consumer, msg)

    except KeyboardInterrupt:
        logger.info("Received shutdown signal")
    except Exception as e:
        logger.error(f"Kafka error: {msg.error().str()} (code: {msg.error().code()})")
    finally:
        consumer.close()
        mongo_client.close()
        logger.info("Product Command Handler stopped")


if __name__ == "__main__":
    asyncio.run(handle_product_commands())