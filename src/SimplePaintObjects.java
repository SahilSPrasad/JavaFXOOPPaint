import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;


public class SimplePaintObjects extends Application {

    static final int CANVAS_HEIGHT = 480;
    static final int CANVAS_WIDTH = 600;

    static final int NUM_POINT_WIDTHS = 4;

    static final Color TOOL_RECT_FG = Color.LIGHTCORAL;
    static final Color TOOL_RECT_BG = Color.WHITE;
    static final Color TOOL_FG = Color.LEMONCHIFFON;
    static final int CELL_W = 50;
    static final int CELL_H = 50;
    static final int PADDING = 5;
    private static double startingX;
    private static double startingY;

    private boolean dragging;

    private double prevX, prevY;


    private final HBox root = new HBox();
    private final VBox tools = new VBox();
    private final VBox colors = new VBox();
    private Canvas canvas;
    private GraphicsContext g;

    ArrayList<ShapeObject> ShapeObjectList = new ArrayList<ShapeObject>();

    private final Color[] palette = {
            Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.CYAN, Color.MAGENTA, Color.color(0.95, 0.9, 0)
    };

    private ColorTool currentColorTool = new ColorTool(null);
    private ShapeTool currentShapeTool = new ShapeTool(null);

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) {
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                null,
                null)));

        root.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        g = canvas.getGraphicsContext2D();

        displayColorAndClearTools();
        displayShapeTools();

        root.getChildren().addAll(canvas, tools, colors);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Simple Paint Objects");
        stage.show();
    }

    private void displayColorAndClearTools() {
        myClearAction();
        createColorTools();
        createClearTool();
    }

    private void displayShapeTools() {
        createPointTools();
        createLineTool();
        createRectangleTool();
        createOvalTool();
        createRoundedRectTool();
    }

    private void myClearAction() {
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        canvas.setOnMousePressed(e -> mousePressed(e));
        canvas.setOnMouseDragged(e -> mouseDragged(e));
        canvas.setOnMouseReleased(e -> mouseReleased(e));
    }

    private void clearButton() {
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        ShapeObjectList.clear();
    }

    private void createColorTools() {
        colors.setPadding(new Insets(0, PADDING, PADDING, PADDING));

        for (int i = 0; i < palette.length; i++) {
            colors.getChildren().addAll(addMouseHandlerToColorTool(
                    new ColorTool(palette[i])
            ));
        }
    }

    private void createClearTool() {
        ActionTool clearBox = new ActionTool(TOOL_RECT_FG, this::clearButton);
        colors.getChildren().addAll(addMouseHandlerToActionTool(clearBox));
    }

    private void createPointTools() {
        int pixelWidth = 2;
        tools.setPadding(new Insets(0, PADDING, PADDING, PADDING));

        for (int i = 0; i < NUM_POINT_WIDTHS; i++) {
            //Create point tools and add them to tools vbox
            PointTool pointBox = new PointTool(TOOL_RECT_FG, pixelWidth);
            tools.getChildren().addAll(addMouseHandlerToShapeTool(pointBox));
            pixelWidth += 2;
        }
    }

    private void createLineTool() {
        LineTool lineBox = new LineTool(
                TOOL_RECT_FG);
        tools.getChildren().addAll(addMouseHandlerToShapeTool(lineBox));
    }

    private void createRectangleTool() {
        RectangleTool rectangleBox = new RectangleTool(
                TOOL_RECT_FG);
        tools.getChildren().addAll(addMouseHandlerToShapeTool(rectangleBox));
    }

    private void createOvalTool() {
        OvalTool ovalBox = new OvalTool(
                TOOL_RECT_FG);
        tools.getChildren().addAll(addMouseHandlerToShapeTool(ovalBox));
    }

    private void createRoundedRectTool() {
        RoundedRectangleTool roundRectBox = new RoundedRectangleTool(
                TOOL_RECT_FG);
        tools.getChildren().addAll(addMouseHandlerToShapeTool(roundRectBox));
    }

    private void drawShapes() {
        for (int i = 0; i < ShapeObjectList.size(); i++) {
            ShapeObjectList.get(i).draw(g);
        }
    }

    public void mousePressed(MouseEvent evt) {
        if (dragging)
            return;

        dragging = true;
        int x = (int) evt.getX();
        int y = (int) evt.getY();
        //System.out.println("canvas clicked");
        prevX = x;
        prevY = y;

        startingX = x;
        startingY = y;
    }

    public void mouseDragged(MouseEvent evt) {
        if (!dragging)
            return;

        double x = evt.getX();
        double y = evt.getY();

        Point2D pointStart = new Point2D(x, y);
        Point2D pointEnd = new Point2D(prevX, prevY);

        myClearAction();
        drawShapes();
        if (currentShapeTool instanceof PointTool) {
            currentShapeTool.draw(g, currentColorTool.getColor(), pointStart,
                    pointEnd);

            if (currentShapeTool.getPaintShape().dragUpdate()) {
                ShapeObjectList.add(currentShapeTool.getPaintShape());
            }
        }

        if (currentShapeTool instanceof LineTool) {
            pointStart = new Point2D(startingX, startingY);
            currentShapeTool.draw(g, currentColorTool.getColor(), pointStart,
                    pointEnd);
        }

        if (currentShapeTool instanceof RectangleTool
                || currentShapeTool instanceof OvalTool
                || currentShapeTool instanceof RoundedRectangleTool) {

            pointStart = new Point2D(startingX, startingY);
            currentShapeTool.draw(g, currentColorTool.getColor(), pointStart,
                    pointEnd);
        }

        prevX = x;
        prevY = y;


    }

    public void mouseReleased(MouseEvent evt) {
        dragging = false;
        ShapeObjectList.add(currentShapeTool.getPaintShape());
    }

    private ColorTool addMouseHandlerToColorTool(ColorTool tool) {
        tool.setOnMousePressed((e) -> {
            this.currentColorTool.deactivate();
            this.currentColorTool = tool;
            tool.activate();
        });
        return tool;
    }

    private ShapeTool addMouseHandlerToShapeTool(ShapeTool tool) {
        tool.setOnMousePressed((e) -> {
            this.currentShapeTool.deactivate();
            this.currentShapeTool = tool;
            tool.activate();
        });
        return tool;
    }

    private ActionTool addMouseHandlerToActionTool(ActionTool tool) {
        tool.setOnMousePressed((e) -> {
            tool.runClearFunction();
        });
        return tool;
    }
}


abstract class AbstractTool extends StackPane {
    private final Rectangle rectangle = new Rectangle();
    private final Double scaleActivationValue = 1.2;

    AbstractTool(Color color) {
        this.setBackground(new Background(new BackgroundFill(Color.WHITE,
                CornerRadii.EMPTY, Insets.EMPTY)));
        this.setPadding(new Insets(
                SimplePaintObjects.PADDING,
                SimplePaintObjects.PADDING,
                SimplePaintObjects.PADDING,
                SimplePaintObjects.PADDING));

        this.rectangle.setFill(color);
        this.rectangle.setWidth(SimplePaintObjects.CELL_W);
        this.rectangle.setHeight(SimplePaintObjects.CELL_H);
        this.getChildren().addAll(rectangle);
    }

    void activate() {
        this.rectangle.setScaleX(scaleActivationValue);
        this.rectangle.setScaleY(scaleActivationValue);
    }

    void deactivate() {
        this.rectangle.setScaleX(1);
        this.rectangle.setScaleY(1);
    }
}


class ShapeTool extends AbstractTool {
    ShapeTool(Color color) {
        super(color);
    }

    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
    }

    public ShapeObject getPaintShape() {
        return null;
    }
}


class ColorTool extends AbstractTool {
    private final Color color;

    public ColorTool(Color color) {
        super(color);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

}


class ActionTool extends AbstractTool {
    private final int fontSize = 14;
    private final Label label = new Label();
    private final Runnable runnable;

    ActionTool(Color color, Runnable runnable) {
        super(color);
        label.setText("Clear");
        label.setTextFill(Color.LEMONCHIFFON);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, fontSize));
        this.getChildren().addAll(label);
        this.runnable = runnable;
    }

    void runClearFunction() {
        runnable.run();
    }
}


class PointTool extends ShapeTool {
    private final Ellipse ellipse = new Ellipse();
    private LineSegmentShape currentLineShape;
    private final int pointPixelAmount;

    PointTool(Color color, int pointPixelAmount) {
        super(color);
        ellipse.setRadiusX(pointPixelAmount);
        ellipse.setRadiusY(pointPixelAmount);
        ellipse.setFill(Color.LEMONCHIFFON);
        this.getChildren().addAll(ellipse);
        this.pointPixelAmount = pointPixelAmount;
    }

    @Override
    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
        currentLineShape = new LineSegmentShape(
                pointPixelAmount, color, start, end);
        currentLineShape.draw(g);
    }

    @Override
    public ShapeObject getPaintShape() {
        return currentLineShape;
    }
}


class LineTool extends ShapeTool {
    private final Line line = new Line();
    private final int lineWidth = 2;
    private LineShape currentLine;
    private final int lineStartX = 0;
    private final int lineStartY = 0;
    private final int lineEndX = 40;
    private final int lineEndY = 40;


    LineTool(Color color) {
        super(color);
        line.setStartX(lineStartX);
        line.setStartY(lineStartY);
        line.setEndX(lineEndX);
        line.setEndY(lineEndY);
        line.setStroke(SimplePaintObjects.TOOL_FG);
        this.getChildren().addAll(line);
    }

    @Override
    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
        currentLine = new LineShape(lineWidth, color, start, end);
        currentLine.draw(g);
    }

    @Override
    public ShapeObject getPaintShape() {
        return currentLine;
    }
}


class RectangleTool extends ShapeTool {
    private final Rectangle rectangleImage = new Rectangle();
    private RectangleShape currentRectangle;
    private final int rectHeight = 35;
    private final int rectWidth= 35;

    RectangleTool(Color color) {
        super(color);
        rectangleImage.setHeight(rectHeight);
        rectangleImage.setWidth(rectWidth);
        rectangleImage.setFill(SimplePaintObjects.TOOL_FG);
        this.getChildren().addAll(rectangleImage);

    }

    @Override
    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
        currentRectangle = new RectangleShape(color, start, end);
        currentRectangle.draw(g);

    }

    @Override
    public ShapeObject getPaintShape() {
        return currentRectangle;
    }
}


class OvalTool extends ShapeTool {
    private final Ellipse ovalImage = new Ellipse();
    private OvalShape currentOval;
    private final int centerX = 0;
    private final int centerY = 0;
    private final int radiusX = 20;
    private final int radiusY= 20;

    OvalTool(Color color) {
        super(color);
        ovalImage.setCenterX(centerX);
        ovalImage.setCenterY(centerY);
        ovalImage.setRadiusX(radiusX);
        ovalImage.setRadiusY(radiusY);
        ovalImage.setFill(SimplePaintObjects.TOOL_FG);
        this.getChildren().addAll(ovalImage);
    }

    @Override
    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
        currentOval = new OvalShape(color, start, end);
        currentOval.draw(g);

    }

    @Override
    public ShapeObject getPaintShape() {
        return currentOval;
    }
}


class RoundedRectangleTool extends ShapeTool {
    private final Rectangle roundedRectImage = new Rectangle();
    private RoundedRectangleShape currentRoundRect;
    private final int roundX = 0;
    private final int roundY = 0;
    private final int roundWidth = 37;
    private final int roundHeight = 37;
    private final int arcHeight = 15;
    private final int arcWidth = 15;


    RoundedRectangleTool(Color color) {
        super(color);
        roundedRectImage.setX(roundX);
        roundedRectImage.setY(roundY);
        roundedRectImage.setWidth(roundWidth);
        roundedRectImage.setHeight(roundHeight);
        roundedRectImage.setFill(SimplePaintObjects.TOOL_FG);
        roundedRectImage.setArcHeight(arcHeight);
        roundedRectImage.setArcWidth(arcWidth);
        this.getChildren().addAll(roundedRectImage);
    }

    @Override
    public void draw(GraphicsContext g, Color color, Point2D start,
                     Point2D end) {
        currentRoundRect = new RoundedRectangleShape(color, start, end);
        currentRoundRect.draw(g);

    }

    @Override
    public ShapeObject getPaintShape() {
        return currentRoundRect;
    }
}

interface ShapeObject {
    void draw(GraphicsContext g);
    boolean dragUpdate();
}

class LineSegmentShape implements ShapeObject {
    private final int width;
    private final Color color;
    private final Point2D start;
    private final Point2D end;

    LineSegmentShape(int width, Color color, Point2D start, Point2D end) {
        this.width = width;
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(GraphicsContext g) {
        g.setLineWidth(width);
        g.setStroke(color);
        g.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    @Override
    public boolean dragUpdate() {
        return true;
    }
}

class LineShape implements ShapeObject {
    private final int width;
    private final Color color;
    private final Point2D start;
    private final Point2D end;

    LineShape(int width, Color color, Point2D start, Point2D end) {
        this.width = width;
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(GraphicsContext g) {
        g.setLineWidth(width);
        g.setStroke(color);
        g.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    @Override
    public boolean dragUpdate() {
        return false;
    }
}

class FilledPolyShape implements ShapeObject {


    @Override
    public void draw(GraphicsContext g) {}

    @Override
    public boolean dragUpdate() {return false;}
}

class RectangleShape extends FilledPolyShape {
    private Color color;
    private Point2D start;
    private Point2D end;
    RectangleShape(Color color, Point2D start, Point2D end) {
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(GraphicsContext g) {
        double distanceX = end.getX() - start.getX();
        double distanceY = end.getY() - start.getY();
        g.setFill(color);
        g.fillRect(start.getX() - distanceX, start.getY() - distanceY,
                distanceX * 2, distanceY * 2);
    }
}

class OvalShape extends FilledPolyShape {
    private Color color;
    private Point2D start;
    private Point2D end;
    OvalShape(Color color, Point2D start, Point2D end) {
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(GraphicsContext g) {
        double distanceX = end.getX() - start.getX();
        double distanceY = end.getY() - start.getY();
        g.setFill(color);
        g.fillOval(start.getX() - distanceX, start.getY() - distanceY,
                distanceX * 2, distanceY * 2);
    }
}

class RoundedRectangleShape extends FilledPolyShape {
    private Color color;
    private Point2D start;
    private Point2D end;
    private final int arcWidth = 50;
    private final int arcHeight = 50;

    RoundedRectangleShape(Color color, Point2D start, Point2D end) {
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(GraphicsContext g) {
        double distanceX = end.getX() - start.getX();
        double distanceY = end.getY() - start.getY();
        g.setFill(color);
        g.fillRoundRect(start.getX() - distanceX, start.getY() - distanceY,
                distanceX * 2, distanceY * 2, arcWidth, arcHeight);
    }
}