package Model;
import lombok.NonNull;

public class Point {

    private float start;
    private float end;

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public Point(@NonNull float start, float end) {
        this.start = start;
        this.end = end;
    }

}
