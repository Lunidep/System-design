Тестировалось так:
# Для Redis
wrk -t10 -c100 -d30s http://localhost:8000/users/admin
# Для PostgreSQL
wrk -t10 -c100 -d30s http://localhost:8000/users/admin?nocache=1

| Количество потоков | RPS (Redis) | RPS (PostgreSQL) | Ускорение |
|--------------------|-------------|------------------|-----------|
| 1                 | 5259.23     | 2760.42          | 1.9x      |
| 5                 | 5245.49     | 2808.27          | 1.87x     |
| 10                | 5319.90     | 2693.24          | 1.98x     |   
