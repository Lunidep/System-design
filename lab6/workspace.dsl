workspace {
    name "Online Shop System v3 (Lab6)"
    !identifiers hierarchical
    description "Архитектура с CQRS для товаров через Kafka и Redis кешированием"

    model {
        user = Person "Пользователь" "Покупатель в интернет-магазине"

        paymentSystem = softwareSystem "Платежная система" {
            description "Внешняя система обработки платежей"
        }

        onlineShop = softwareSystem "Интернет-магазин (Lab6)" {
            description "Система с CQRS для товаров через Kafka, Redis кешированием и MongoDB"

            -> paymentSystem "Обрабатывает платежи через API"

            apiGateway = container "API Gateway" {
                technology "Python + FastAPI"
                description "Единая точка входа для всех запросов"
                tags "entry-point"
            }

            userService = container "User Service" {
                technology "Python + FastAPI"
                description "Управление пользователями с Redis кешированием"
                tags "user-management caching"
            }

            productService = container "Product Service (CQRS)" {
                technology "Python + FastAPI"
                description "Управление товарами (командная часть CQRS)"
                tags "product-management cqrs-command"

                component "Product API" "Предоставляет API для управления продуктами"
                component "Command Producer" "Отправляет команды в Kafka"
            }

            productQueryService = container "Product Query Service" {
                technology "Python + FastAPI"
                description "Чтение данных о товарах (чтение в CQRS)"
                tags "product-management cqrs-query"
            }

            productCommandHandler = container "Product Command Handler" {
                technology "Python"
                description "Обработчик команд из Kafka для товаров"
                tags "product-management cqrs-processor"
            }

            cartService = container "Cart Service" {
                technology "Python + FastAPI"
                description "Управление корзинами покупок"
                tags "cart-management"
            }

            userDatabase = container "PostgreSQL (Users)" {
                technology "PostgreSQL 14"
                description "Основное хранилище пользовательских данных"
                tags "storage relational-db"
            }

            productDatabase = container "MongoDB (Products)" {
                technology "MongoDB 5.0"
                description "Хранилище товаров (обновляется через команды)"
                tags "storage nosql cqrs"
            }

            cartDatabase = container "PostgreSQL (Carts)" {
                technology "PostgreSQL 14"
                description "Хранилище корзин пользователей"
                tags "storage relational-db"
            }

            cache = container "Redis Cache" {
                technology "Redis 7"
                description "Кеш для пользовательских данных и результатов поиска"
                tags "caching"
            }

            messageBroker = container "Apache Kafka" {
                technology "Kafka"
                description "Брокер сообщений для команд CQRS"
                tags "messaging cqrs"
            }
        }

        /* Связи */
        user -> onlineShop.apiGateway "Просмотр/покупка товаров через UI/API"

        onlineShop.apiGateway -> onlineShop.userService "API вызовы"
        onlineShop.apiGateway -> onlineShop.productService "API вызовы (команды)"
        onlineShop.apiGateway -> onlineShop.productQueryService "API вызовы (запросы)"
        onlineShop.apiGateway -> onlineShop.cartService "API вызовы"

        onlineShop.userService -> onlineShop.userDatabase "CRUD операции"
        onlineShop.userService -> onlineShop.cache "Кеширование данных"

        onlineShop.productService -> onlineShop.messageBroker "Отправка команд"
        onlineShop.productQueryService -> onlineShop.productDatabase "Чтение данных"
        onlineShop.productCommandHandler -> onlineShop.messageBroker "Чтение команд"
        onlineShop.productCommandHandler -> onlineShop.productDatabase "Обновление данных"

        onlineShop.cartService -> onlineShop.cartDatabase "CRUD операции"

        /* Deployment */
        deploymentEnvironment "PROD" {
            deploymentNode "DMZ" {
                deploymentNode "web-app.onlineshop.ru" {
                    containerInstance onlineShop.apiGateway
                }
            }

            deploymentNode "PROTECTED" {
                deploymentNode "k8s-cluster" {
                    infrastructureNode "LoadBalancer" -> "pod-user" "ЛБ трафик"

                    deploymentNode "pod-user" {
                        containerInstance onlineShop.userService
                        instances 3
                    }

                    deploymentNode "pod-product" {
                        containerInstance onlineShop.productService
                        containerInstance onlineShop.productQueryService
                        instances 2
                    }

                    deploymentNode "pod-cart" {
                        containerInstance onlineShop.cartService
                    }

                    deploymentNode "pod-background" {
                        containerInstance onlineShop.productCommandHandler
                        instances 2
                    }

                    deploymentNode "message-broker" {
                        containerInstance onlineShop.messageBroker
                    }

                    deploymentNode "db-primary" {
                        containerInstance onlineShop.userDatabase
                        containerInstance onlineShop.cartDatabase
                    }

                    deploymentNode "db-secondary" {
                        containerInstance onlineShop.productDatabase
                        containerInstance onlineShop.cache
                    }
                }
            }
        }
    }

    views {
        themes default

        /* Диаграммы контекста */
        systemContext onlineShop "SystemContext" {
            include *
            autoLayout lr
        }

        /* Контейнерная диаграмма */
        container onlineShop "Containers" {
            include *
            autoLayout tb
        }

        /* Диаграмма развертывания */
        deployment onlineShop "Deployment" {
            environment "PROD"
            include *
            autoLayout
        }

        /* Сценарии работы */
        dynamic onlineShop "Product_Create_Flow" {
            description "Процесс создания товара через CQRS"
            user -> onlineShop.apiGateway "POST /products"
            onlineShop.apiGateway -> onlineShop.productService "Создание товара"
            onlineShop.productService -> onlineShop.messageBroker "Отправка команды"
            onlineShop.productCommandHandler -> onlineShop.messageBroker "Чтение команды"
            onlineShop.productCommandHandler -> onlineShop.productDatabase "Сохранение в MongoDB"
        }

        dynamic onlineShop "Product_Read_Flow" {
            description "Чтение данных о товарах"
            user -> onlineShop.apiGateway "GET /products"
            onlineShop.apiGateway -> onlineShop.productQueryService "Запрос товаров"
            onlineShop.productQueryService -> onlineShop.productDatabase "Чтение из MongoDB"
        }

        dynamic onlineShop "User_Login_Flow" {
            description "Процесс аутентификации с кешированием"
            user -> onlineShop.apiGateway "POST /token"
            onlineShop.apiGateway -> onlineShop.userService "Проверка учетных данных"
            onlineShop.userService -> onlineShop.cache "Проверка кеша"
            onlineShop.userService -> onlineShop.userDatabase "Запрос если нет в кеше"
            onlineShop.userService -> onlineShop.cache "Кеширование результата"
        }
    }

    styles {
        element "Person" {
            background #FFA07A
            shape person
        }
        element "Cache" {
            background #FFD700
            shape cylinder
        }
        element "NoSQL" {
            border dashed
        }
        element "CQRS-Command" {
            background #FF6347
        }
        element "CQRS-Query" {
            background #98FB98
        }
        element "Messaging" {
            shape ellipse
            background #ADD8E6
        }
    }
}