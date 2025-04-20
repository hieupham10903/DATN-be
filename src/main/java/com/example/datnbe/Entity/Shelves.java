package com.example.datnbe.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shelves")
public class Shelves {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "warehouse_id")
    private String warehouseId;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarehouseId() {
        return this.warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
}
