package com.ecnu.adsmls.components.editor.treeeditor.impl;

import com.ecnu.adsmls.components.editor.treeeditor.TreeArea;
import com.ecnu.adsmls.utils.Position;
import com.ecnu.adsmls.utils.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BranchPoint extends TreeArea {
    private final double r = TreeAreaRadius.BranchPoint.getR();

    public BranchPoint(long id, Position position) {
        super(id, position);
        this.position.x -= this.r;
        this.position.y -= this.r;

        this.createNode();
        this.inactive();
        this.enableDrag();
    }

    @Override
    public Position getLinkPoint(Position adjacentPoint) {
        Position center = getCenterPoint();
        Vector2D vector = new Vector2D(center, adjacentPoint);
        double rad = vector.radWithXAxis();
        double x = this.r * Math.cos(rad);
        double y = -this.r * Math.sin(rad);
        return new Position(center.x + x, center.y + y);
    }

    @Override
    public Position getCenterPoint() {
        double x = this.position.x + this.r;
        double y = this.position.y + this.r;
        return new Position(x, y);
    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void inactive() {
        super.inactive();
    }

    @Override
    public void createNode() {
        Circle circle = new Circle();
        circle.setCenterX(this.position.x + this.r);
        circle.setCenterY(this.position.y + this.r);
        circle.setRadius(this.r);
        circle.setFill(Color.WHITE);
        circle.setStrokeWidth(2);
        circle.setSmooth(true);
        circle.getStrokeDashArray().addAll(3.0, 3.0);
        this.shape = circle;
    }

    @Override
    public void updatePosition() {
        ((Circle) this.shape).setCenterX(this.position.x + this.r);
        ((Circle) this.shape).setCenterY(this.position.y + this.r);
    }

    @Override
    public void updateNode() {
        this.updatePosition();
        this.addNode(this.shape);
    }
}
