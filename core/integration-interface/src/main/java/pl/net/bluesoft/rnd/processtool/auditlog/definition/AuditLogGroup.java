package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-13
 */
public class AuditLogGroup {
	private final String groupKey;
	private final String messageKey;

	private final Map<String, SimpleAuditConfig> simpleAttrConfigs = new LinkedHashMap<String, SimpleAuditConfig>();
	private final Map<Class, AuditedEntityHandler> entityConfigs = new LinkedHashMap<Class, AuditedEntityHandler>();

	public AuditLogGroup(String groupKey) {
		this(groupKey, groupKey);
	}

	public AuditLogGroup(String groupKey, String messageKey) {
		this.groupKey = groupKey;
		this.messageKey = messageKey;
	}

	public String getGroupKey() {
		return groupKey;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public AuditLogGroup add(String attributeName) {
		return add(new SimpleAuditConfig(attributeName));
	}

	public AuditLogGroup add(String attributeName, String dictKey) {
		return add(attributeName, StringUtils.isNotEmpty(dictKey) ? new DirectDictResolver(dictKey) : null);
	}

	public AuditLogGroup add(String attributeName, DictResolver dictResolver) {
		return add(new SimpleAuditConfig(attributeName, dictResolver));
	}

	private AuditLogGroup add(SimpleAuditConfig config) {
		if (simpleAttrConfigs.containsKey(config.attributeName)) {
			throw new IllegalArgumentException("Group " + groupKey + " has already attribute " + config);
		}
		simpleAttrConfigs.put(config.attributeName, config);
		return this;
	}

	public <T> AuditLogGroup add(Class<T> entityClass, AuditedEntityHandler<T> handler) {
		if (entityConfigs.containsKey(entityClass)) {
			throw new IllegalArgumentException("Group " + groupKey + " has already entity " + entityClass);
		}
		entityConfigs.put(entityClass, handler);
		return this;
	}

	public boolean supports(String key) {
		return getAuditConfig(key) != null;
	}

	public boolean supports(Class<? extends AbstractPersistentEntity> entityClass) {
		return getAuditedEntityHandler(entityClass) != null;
	}

	public SimpleAuditConfig getAuditConfig(String key) {
		return simpleAttrConfigs.get(key);
	}

	public <T extends AbstractPersistentEntity> AuditedEntityHandler getAuditedEntityHandler(Class<T> entityClass) {
		for (Map.Entry<Class, AuditedEntityHandler> entry : entityConfigs.entrySet()) {
			if (entry.getKey().isAssignableFrom(entityClass)) {
				return entry.getValue();
			}
		}
		return null;
	}
}
