package us.mcparks.achievables.dsl.meta

interface MetaBuilder<T extends AchievableMeta> {
    T build();
}