INSERT INTO ORDERS (product_id, quantity, total_price, customer_name, customer_email, order_date, status) VALUES
(1, 1, 1200000.0, '홍길동', 'hong@example.com', CURRENT_TIMESTAMP(), 'COMPLETED'),
(2, 2, 1600000.0, '김철수', 'kim@example.com', CURRENT_TIMESTAMP(), 'PENDING'),
(3, 1, 300000.0, '이영희', 'lee@example.com', CURRENT_TIMESTAMP(), 'PROCESSING'); 