import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyAnalyzer {

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("package\\s+([a-zA-Z_][\\.\\w]*);");
    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("import\\s+([a-zA-Z_][\\.\\w]*);");

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: <path-to-source> <root-package> <dependency-root-package>");
            return;
        }

        Path rootPath = Paths.get(args[0]);
        String rootPackage = args[1];
        String dependencyRootPackage = args[2];

        DependencyAnalyzer analyzer = new DependencyAnalyzer();
        Map<String, List<String>> dependencies =
                analyzer.findDependencies(rootPath, rootPackage, dependencyRootPackage);

        dependencies.forEach((pkg, deps) -> {
            System.out.println(pkg);
            deps.forEach(dep -> System.out.println("  -> " + dep));
        });
    }

    public Map<String, List<String>> findDependencies(Path rootPath, String rootPackage,
            String dependencyRootPackage) throws IOException {
        Map<String, List<String>> dependencyMap = new HashMap<>();

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> processJavaFile(path, rootPackage, dependencyRootPackage,
                            dependencyMap));
        }

        return dependencyMap;
    }

    private void processJavaFile(Path path, String rootPackage, String dependencyRootPackage,
            Map<String, List<String>> dependencyMap) {
        try {
            String content = Files.readString(path);
            String packageName = extractPackageName(content);
            if (packageName.startsWith(rootPackage)) {
                List<String> dependentPackages =
                        extractDependentPackages(content, dependencyRootPackage);

                if (!dependentPackages.isEmpty()) {
                    dependencyMap.put(packageName, dependentPackages);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> extractDependentPackages(String content, String dependencyRootPackage) {
        return extractWithPattern(content, IMPORT_PATTERN)
                .filter(imp -> imp.startsWith(dependencyRootPackage))
                .map(imp -> imp.substring(0, imp.lastIndexOf('.'))) // Remove class name
                .distinct().collect(Collectors.toList());
    }

    private String extractPackageName(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private Stream<String> extractWithPattern(String content, Pattern pattern) {
        return pattern.matcher(content).results().map(MatchResult::group)
                .map(group -> group.substring(group.lastIndexOf(' ') + 1, group.length() - 1));
    }
}
