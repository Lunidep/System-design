workspace {
    name "Online Shop System"
    !identifiers hierarchical

    model {

        user = Person "Пользователь" "Человек, который взаимодействует с системой для покупки товаров."

        paymentSystem = softwareSystem "Платежная система" "Система, которая интегрируется с магазином для обработки платежей."

        onlineShop = softwareSystem "Система интернет-магазина" {
            -> paymentSystem "Обработка платежей"

            apiGateway = container "API Gateway" {
                technology "Python"
                tags "entry-point"
            }

            userService = container "Пользовательский сервис" {
                technology "Python"
                tags "user-management"
            }

            productService = container "Товарный сервис" {
                technology "Python"
                tags "product-management"
            }

            cartService = container "Сервис корзины" {
                technology "Python"
                tags "cart-management"
            }

            userDatabase = container "База данных пользователей" {
                technology "PostgreSQL"
                tags "storage"
            }

            productDatabase = container "База данных товаров" {
                technology "PostgreSQL"
                tags "storage"
            }

            cartDatabase = container "База данных корзин" {
                technology "PostgreSQL"
                tags "storage"
            }
        }

        user -> onlineShop.apiGateway "HTTP/HTTPS"
        paymentSystem -> onlineShop.apiGateway "HTTP/HTTPS"

        onlineShop.apiGateway -> onlineShop.userService "HTTP/HTTPS"
        onlineShop.apiGateway -> onlineShop.productService "HTTP/HTTPS"
        onlineShop.apiGateway -> onlineShop.cartService "HTTP/HTTPS"

        onlineShop.userService -> onlineShop.userDatabase "CRUD операции"
        onlineShop.productService -> onlineShop.productDatabase "CRUD операции"
        onlineShop.cartService -> onlineShop.cartDatabase "CRUD операции"

        deploymentEnvironment "PROD" {
            deploymentNode "DMZ" {
                deploymentNode "web-app.onlineshop.ru" {
                    containerInstance onlineShop.apiGateway
                }
            }

            deploymentNode "PROTECTED" {
                deploymentNode "k8.namespace" {
                    lb = infrastructureNode "LoadBalancer"

                    pod1 = deploymentNode "pod1" {
                        us = containerInstance onlineShop.userService
                        instances 5
                    }
                    pod2 = deploymentNode "pod2" {
                        ps = containerInstance onlineShop.productService
                        instances 3
                    }
                    pod3 = deploymentNode "pod3" {
                        ud = containerInstance onlineShop.userDatabase
                        pd = containerInstance onlineShop.productDatabase
                        cd = containerInstance onlineShop.cartDatabase
                    }
                    pod4 = deploymentNode "pod4" {
                        cs = containerInstance onlineShop.cartService
                    }

                    lb -> pod1.us "Send requests"
                }
            }
        }
    }

    views {

        themes default

        systemContext onlineShop "context" {
            include *
            autoLayout lr
        }

        container onlineShop "c2" {
            include *
            autoLayout
        }

        deployment * "PROD" {
            include *
            autoLayout
        }

        dynamic onlineShop "Create_New_User" "Описывает процесс создания нового пользователя." {
            autoLayout lr
            user -> onlineShop.apiGateway "POST /users"
            onlineShop.apiGateway -> onlineShop.userService "POST /createUser"
            onlineShop.userService -> onlineShop.userDatabase "INSERT INTO users"
        }

        dynamic onlineShop "Find_User_By_Login" "Описывает процесс поиска пользователя по логину." {
            autoLayout lr
            user -> onlineShop.apiGateway "GET /users?login={login}"
            onlineShop.apiGateway -> onlineShop.userService "GET /findUserByLogin"
            onlineShop.userService -> onlineShop.userDatabase "SELECT FROM users WHERE login = ?"
        }

        dynamic onlineShop "Find_User_By_Name_Mask" "Описывает процесс поиска пользователя по маске имени и фамилии." {
            autoLayout lr
            user -> onlineShop.apiGateway "GET /users?nameMask={mask}"
            onlineShop.apiGateway -> onlineShop.userService "GET /findUserByNameMask"
            onlineShop.userService -> onlineShop.userDatabase "SELECT FROM users WHERE name LIKE ?"
        }

        dynamic onlineShop "Create_Product" "Описывает процесс создания товара." {
            autoLayout lr
            user -> onlineShop.apiGateway "POST /products"
            onlineShop.apiGateway -> onlineShop.productService "POST /createProduct"
            onlineShop.productService -> onlineShop.productDatabase "INSERT INTO products"
        }

        dynamic onlineShop "Get_Product_List" "Описывает процесс получения списка товаров." {
            autoLayout lr
            user -> onlineShop.apiGateway "GET /products"
            onlineShop.apiGateway -> onlineShop.productService "GET /listProducts"
            onlineShop.productService -> onlineShop.productDatabase "SELECT FROM products"
        }

        dynamic onlineShop "Add_Product_To_Cart" "Описывает процесс добавления товара в корзину." {
            autoLayout lr
            user -> onlineShop.apiGateway "POST /cart/add"
            onlineShop.apiGateway -> onlineShop.cartService "POST /addProductToCart"
            onlineShop.cartService -> onlineShop.cartDatabase "INSERT INTO cart"
        }

        dynamic onlineShop "Get_Cart_For_User" "Описывает процесс получения корзины для пользователя." {
            autoLayout lr
            user -> onlineShop.apiGateway "GET /cart"
            onlineShop.apiGateway -> onlineShop.cartService "GET /getCartForUser"
            onlineShop.cartService -> onlineShop.cartDatabase "SELECT FROM cart WHERE user_id = ?"
        }
    }
}