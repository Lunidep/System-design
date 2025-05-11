workspace {
    name "Online Shop System v2 (Lab5)"
    !identifiers hierarchical
    description "Обновленная архитектура с Redis кешированием и MongoDB для товаров"

    model {
        user = Person "Пользователь" "Покупатель в интернет-магазине"
        
        paymentSystem = softwareSystem "Платежная система" {
            description "Внешняя система обработки платежей"
        }

        onlineShop = softwareSystem "Интернет-магазин (Lab5)" {
            description "Система с Redis кешированием и MongoDB хранилищем"
            
            -> paymentSystem "Обрабатывает платежи через API"
            
            apiGateway = container "API Gateway" {
                technology "Python + FastAPI"
                description "Единая точка входа для всех запросов"
                tags "entry-point"
            }

            userService = container "User Service (Lab5)" {
                technology "Python + FastAPI"
                description "Управление пользователями с Redis кешированием"
                tags "user-management caching"
            }

            productService = container "Product Service (Lab5)" {
                technology "Python + FastAPI"
                description "Управление товарами с MongoDB"
                tags "product-management nosql"
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
                description "Хранилище товаров с полнотекстовым поиском"
                tags "storage nosql"
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
        }

        /* Связи */
        user -> onlineShop.apiGateway "Просмотр/покупка товаров через UI/API"
        
        onlineShop.apiGateway -> onlineShop.userService "API вызовы"
        onlineShop.apiGateway -> onlineShop.productService "API вызовы"
        onlineShop.apiGateway -> onlineShop.cartService "API вызовы"

        onlineShop.userService -> onlineShop.userDatabase "CRUD операции"
        onlineShop.userService -> onlineShop.cache "Кеширование данных"
        
        onlineShop.productService -> onlineShop.productDatabase "CRUD операции"
        
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
                        instances 2
                    }
                    
                    deploymentNode "pod-cart" {
                        containerInstance onlineShop.cartService
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
        dynamic onlineShop "User_Login_Flow" {
            description "Процесс аутентификации с кешированием"
            user -> onlineShop.apiGateway "POST /token"
            onlineShop.apiGateway -> onlineShop.userService "Проверка учетных данных"
            onlineShop.userService -> onlineShop.cache "Проверка кеша"
            onlineShop.userService -> onlineShop.userDatabase "Запрос если нет в кеше"
            onlineShop.userService -> onlineShop.cache "Кеширование результата"
        }

        dynamic onlineShop "Product_Search_Flow" {
            description "Поиск товаров в MongoDB"
            user -> onlineShop.apiGateway "GET /products/search"
            onlineShop.apiGateway -> onlineShop.productService "Полнотекстовый поиск"
            onlineShop.productService -> onlineShop.productDatabase "Запрос к MongoDB"
        }

        dynamic onlineShop "Get_User_Profile_Cached" {
            description "Получение профиля с кешированием"
            user -> onlineShop.apiGateway "GET /users/{id}"
            onlineShop.apiGateway -> onlineShop.userService "Запрос пользователя"
            onlineShop.userService -> onlineShop.cache "Попытка получить из Redis"
            onlineShop.userService -> onlineShop.userDatabase "Запрос если нет в кеше"
            onlineShop.userService -> onlineShop.cache "Сохранение в кеш"
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
    }
}