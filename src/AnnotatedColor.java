import java.awt.Color;
import java.awt.color.ColorSpace;

public class AnnotatedColor extends Color {

	private String filename;
	private String comment;
	private String location;
	private String time;
	
	public AnnotatedColor(int rgb, String filename, String comment, String location, String time) {
		super(rgb);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;
		
	}

	public AnnotatedColor(int rgba, boolean hasalpha, String filename, String comment, String location, String time) {
		super(rgba, hasalpha);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}

	public AnnotatedColor(int r, int g, int b, String filename, String comment, String location, String time) {
		super(r, g, b);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}

	public AnnotatedColor(float r, float g, float b, String filename, String comment, String location, String time) {
		super(r, g, b);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}

	public AnnotatedColor(ColorSpace cspace, float[] components, float alpha, String filename, String comment, String location, String time) {
		super(cspace, components, alpha);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}

	public AnnotatedColor(int r, int g, int b, int a, String filename, String comment, String location, String time) {
		super(r, g, b, a);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}

	public AnnotatedColor(float r, float g, float b, float a, String filename, String comment, String location, String time) {
		super(r, g, b, a);
		this.filename = filename;
		this.comment = comment;
		this.location = location;
		this.time = time;	}
	
	String getComment() {
		return comment;
	}
	
	String getFilename() {
		return filename;
	}
	
	String getLocation() {
		return location;
	}
	
	String getTime() {
		return time;
	}

}
