package us.mcparks.achievables.dsl

// We use this to extract the syntax version so we know which version of the DSL to use
public class VersionDsl {
    def data = [:]
    static int achievement(final Closure closure) {
        VersionDsl versionDsl = new VersionDsl()
        closure.delegate = versionDsl
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return versionDsl.data["syntaxVersion"] as int

    }

    def methodMissing(String name, args) {
        data[name] = args.size() == 1 ? args[0] : args
    }

}
