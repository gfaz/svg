package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.IntArray;

import nu.xom.Attribute;

/** tags SVG primitives as geometric shapes.
 * <p>
 * Essentially implements Java2D.Shape
 * </p>
 * @author pm286
 *
 */
public abstract class SVGShape extends SVGElement {
	private static final Logger LOG = Logger.getLogger(SVGShape.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final String ALL_SHAPE_XPATH = "" +
			".//svg:circle[not(ancestor::svg:defs)] | " +
			".//svg:ellipse[not(ancestor::svg:defs)] | " +
			".//svg:image[not(ancestor::svg:defs)] | " +
			".//svg:line[not(ancestor::svg:defs)] | " +
			".//svg:path[not(ancestor::svg:defs)] | " +
			".//svg:polygon[not(ancestor::svg:defs)] | " +
			".//svg:polyline[not(ancestor::svg:defs)] | " +
			".//svg:rect[not(ancestor::svg:defs)]" +
			"";

	protected SVGShape(String name) {
		super(name);
	}

	protected SVGShape(SVGElement element) {
		super(element);
	}

	/** a string that uniquely defines the geometric position without attributes.
	 * 
	 * Fairly crude. Object is to identify precise duplicates of the object.
	 * Requires exact consistent formatting of the coordinates.
	 * 
	 * @return
	 */
	public abstract String getGeometricHash();
	
	/** makes a new list composed of the shapes in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGShape> extractShapes(List<SVGElement> elements) {
		List<SVGShape> shapeList = new ArrayList<SVGShape>();
		for (SVGElement element : elements) {
			if (element instanceof SVGShape) {
				shapeList.add((SVGShape) element);
			}
		}
		return shapeList;
	}

	/** convenience method to extract list of svgShapes in element
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGShape> extractSelfAndDescendantShapes(SVGElement svgElement) {
		return SVGShape.extractShapes(SVGUtil.getQuerySVGElements(svgElement, ALL_SHAPE_XPATH));
	}

	private static void replaceLineAndCloseUp(int iline, SVGLine newLine, List<SVGLine> lineListNew) {
		lineListNew.set(iline, newLine);
		lineListNew.remove(iline + 1);
	}

	public String getSignature() {
		return getGeometricHash();
	}

	public void setMarkerEndRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerEnd(makeUrlRef(id));
	}

	private void setMarkerEnd(String markerEnd) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_END, markerEnd));
	}

	public void setMarkerStartRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerStart(makeUrlRef(id));
	}

	private void setMarkerStart(String markerStart) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_START, markerStart));
	}

	public void setMarkerMidRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerMid(makeUrlRef(id));
	}

	private String makeUrlRef(String id) {
		return "url(#"+id+")";
	}

	private void setMarkerMid(String markerMid) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_MID, markerMid));
	}

	/** is this an zero-dimensional shape?
	 * 
	 * @return
	 */
	public boolean isZeroDimensional() {
		if (boundingBox == null) {
			getBoundingBox();
		}
		boolean isZeroDimensional = boundingBox == null ||
			(boundingBox.getXRange().getRange() == 0.0 &&
			boundingBox.getYRange().getRange() == 0.0);
		return isZeroDimensional;
	}

	public static void eliminateGeometricalDuplicates(List<? extends SVGShape> shapes, double epsilon) {
		List<SVGShape> uniqueShapes = new ArrayList<SVGShape>();
		for (int i = shapes.size() - 1; i >= 0; i--) {
			SVGShape shape = shapes.get(i);
			if (SVGShape.indexOfGeometricalEquivalent(uniqueShapes, shape, epsilon) != -1) {
				shapes.remove(i);
			} else {
				uniqueShapes.add(shape);
			}
		}
	}

	public static int indexOfGeometricalEquivalent(List<SVGShape> shapes, SVGShape shape, double epsilon) {
		int index = -1;
		if (shape != null) {
			for (int i = 0; i < shapes.size(); i++) {
				SVGShape shape1 = shapes.get(i);
				if (shape1 != null && shape1.isGeometricallyEqualTo(shape, epsilon)) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	/** are two shapes geometrically equal?
	 * are 2 shapes equal within a tolerance? display attributes are ignored.
	 * 
	 * @param shape to compare with
	 * @param epsilon tolerance
	 * @return equivalence
	 */
	protected abstract boolean isGeometricallyEqualTo(SVGShape shape, double epsilon);

	public void addTitle(String title) {
		SVGTitle svgTitle = new SVGTitle(title);
		this.appendChild(svgTitle);
	}

	/** indexes of all elements matching shape geometrically
	 * uses @ isGeometricallyEqualTo(SVGShape shape, double epsilon);
	 * 
	 * @param shapes list to search
	 * @param shape shape to search for 
	 * @param epsilon tolerance
	 * @return empty array if none, else serial numbers
	 */
	public static IntArray indexesOfGeometricalEquivalent(List<SVGShape> shapes, SVGShape shape, double epsilon) {
		IntArray intArray = new IntArray();
		if (shapes != null && shape != null) {
			for (int i = 0; i < shapes.size(); i++) {
				if (shapes.get(i).isGeometricallyEqualTo(shape, epsilon)) {
					intArray.addElement(i);
				}
			}
		}
		return intArray;
	}

	
}
