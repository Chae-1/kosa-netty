import java.util.LinkedHashMap;

public class Test {

    public static void main(String[] args) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String s = map.computeIfAbsent("s", (key) -> {
            System.out.println(key);
            return "a";
        });

        System.out.println(map.get("s"));
        System.out.println(s);
    }
}
