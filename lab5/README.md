Тестировалось так:
# Для Redis
wrk -t10 -c100 -d30s http://localhost:8000/users/admin
# Для PostgreSQL
wrk -t10 -c100 -d30s http://localhost:8000/users/admin?nocache=1

| Количество потоков | RPS (Redis) | RPS (PostgreSQL) | Ускорение |
|--------------------|-------------|------------------|-----------|
| 1                 | 5133.23     | 2968.24          | 1,72x      |
| 5                 | 5213.66     | 2799.54          | 1.86x     |
| 10                | 5411.40     | 2655.34          | 2.03x     |   
