package Model;

import java.util.HashSet;

public class VideoStream {

    private Point currentPoint;
    private float longestValidTimeframe = 0;
    private HashSet<Point> validPoints = new HashSet<>();
    private float videoDuration;
    private float totalInvalidFreezeDuration = 0;

    public VideoStream(){
        currentPoint = new Point(0, 0);
    }

    public void setVideoDuration(float videoDuration) {
        this.videoDuration = videoDuration;
    }

    public float getTotalInvalidFreezeDuration() {
        return totalInvalidFreezeDuration;
    }

    public void setTotalInvalidFreezeDuration(float totalInvalidFreezeDuration) {
        this.totalInvalidFreezeDuration = totalInvalidFreezeDuration;
    }

    public HashSet<Point> getValidPoints() {
        return validPoints;
    }

    public float getVideoDuration() {
        return videoDuration;
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    public float getLongestValidTimeframe() {
        return longestValidTimeframe;
    }

    public void setLongestValidTimeframe(float longestValidTimeframe) {
        this.longestValidTimeframe = longestValidTimeframe;
    }

}
