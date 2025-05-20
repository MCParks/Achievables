package us.mcparks.achievables.dsl.meta

interface MetaBuilder<T extends AchievableMeta> {
    T build();
}

class DefaultMetaBuilder implements MetaBuilder<AchievableMeta> {
    private String name = ""
    private String description = ""
    
    void name(String name) {
        this.name = name
    }
    
    void description(String description) {
        this.description = description
    }
    
    @Override
    AchievableMeta build() {
        return new SimpleAchievableMeta(name, description)
    }
    
    static class SimpleAchievableMeta implements AchievableMeta {
        private final String name
        private final String description
        
        SimpleAchievableMeta(String name, String description) {
            this.name = name
            this.description = description
        }
        
        @Override
        String getName() {
            return name
        }
        
        @Override
        String getDescription() {
            return description
        }
    }
}