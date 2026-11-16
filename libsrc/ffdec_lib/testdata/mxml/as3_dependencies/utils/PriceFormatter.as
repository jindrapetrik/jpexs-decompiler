package utils {
    public class PriceFormatter {
        public static function format(value:Number):String { return value.toFixed(2) + CurrencySymbol.VALUE; }
    }
}
