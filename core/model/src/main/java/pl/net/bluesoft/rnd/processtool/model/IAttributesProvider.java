package pl.net.bluesoft.rnd.processtool.model;

/**
 * Created by pkuciapski on 2014-04-29.
 */
public interface IAttributesProvider {
    ProcessInstance getProcessInstance();

    String getSimpleAttributeValue(String key);

	String getSimpleLargeAttributeValue(String key);

    String getExternalKey();

    String getDefinitionName();

    Object getAttribute(String key);

    Object getProvider();

    Long getId();
}
