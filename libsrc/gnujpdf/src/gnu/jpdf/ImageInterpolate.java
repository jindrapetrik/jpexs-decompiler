package gnu.jpdf;

import java.awt.Image;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class ImageInterpolate {
    public Image image;
    public boolean interpolate;

    public ImageInterpolate(Image image, boolean interpolate) {
        this.image = image;
        this.interpolate = interpolate;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.image);
        hash = 89 * hash + (this.interpolate ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageInterpolate other = (ImageInterpolate) obj;
        if (this.interpolate != other.interpolate) {
            return false;
        }
        return Objects.equals(this.image, other.image);
    }       
}
