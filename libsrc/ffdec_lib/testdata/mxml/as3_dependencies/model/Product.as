package model {
    import utils.PriceFormatter;
    [Bindable]
    public class Product extends Entity implements IPriced {
        public var price:Number = 10;
        public var related:RelatedProduct;
        public function Product() { related = new RelatedProduct(this); }
        public function amount():Number { return price; }
        public function formatted():String { return PriceFormatter.format(price); }
    }
}
