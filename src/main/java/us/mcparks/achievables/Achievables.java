package us.mcparks.achievables;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import us.mcparks.achievables.dsl.BigalsIntegratedGroovyAchievementLanguage;
import us.mcparks.achievables.dsl.meta.MetaBuilder;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class Achievables {
    @Getter
    private AchievableManager achievableManager;

    @Getter @Setter
    private Logger logger;

    @Getter
    private static Achievables instance;

    public Achievables(AchievableManager achievableManager) {
        instance = this;
        this.achievableManager = achievableManager;
        this.logger = Logger.getGlobal();
    }

    public void setAchievableMetaBuilderSupplier(Supplier<MetaBuilder<?>> builder) {
        BigalsIntegratedGroovyAchievementLanguage.setMetaBuilderSupplier(builder);
    }

    public static void initialize(AchievableManager achievableManager) {
        instance = new Achievables(achievableManager);
    }

    public static void customizeGson(Consumer<GsonBuilder> transformer) {
        AchievableGsonManager.transformGson(transformer);
    }


}
