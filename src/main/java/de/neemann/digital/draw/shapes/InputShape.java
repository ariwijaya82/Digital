package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.SingleValueDialog;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.RAD;
import static de.neemann.digital.draw.shapes.OutputShape.SIZE;

/**
 * The input shape
 *
 * @author hneemann
 */
public class InputShape implements Shape {

    private final String label;
    private final PinDescriptions outputs;
    private IOState ioState;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public InputShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getOutput(0).addObserverToValue(guiObserver);
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    modelSync.access(() -> {
                        if (value.supportsHighZ()) {
                            if (value.isHighZ()) value.set(0, false);
                            else if (value.getValue() == 0) value.setValue(1);
                            else value.set(0, true);
                        } else
                            value.setValue(1 - value.getValue());
                    });
                } else {
                    SingleValueDialog.editValue(pos, value, modelSync);
                }
                return true;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        Style style = Style.NORMAL;
        if (ioState != null) {
            ObservableValue value = ioState.getOutput(0);
            style = Style.getWireStyle(value);
            if (value.getBits() > 1) {
                Vector textPos = new Vector(-1 - SIZE, -4 - SIZE);
                graphic.drawText(textPos, textPos.add(1, 0), value.getValueString(), Orientation.CENTERBOTTOM, Style.NORMAL);
            }
        }

        Vector center = new Vector(-1 - SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 1, -SIZE).add(-1, -SIZE).add(-1, SIZE).add(-SIZE * 2 - 1, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
