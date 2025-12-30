package com.example.cust.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class ProductMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int quantity;
    private String productCode;
    private String content;

    @Column(unique = true, nullable = false)
    private String messageId = UUID.randomUUID().toString();


    public ProductMessage() {}

    public ProductMessage(String title, int quantity, String productCode, String content) {
        this.title = title;
        this.quantity = quantity;
        this.productCode = productCode;
        this.content = content;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}

