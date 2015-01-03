package com.boundary.metrics.vmware.poller;

public class MetricDefinitionBuilder extends MetricDefinition {

	
	public MetricDefinitionBuilder() {
		
	}
	
	public MetricDefinitionBuilder setMetric(String metric) {
		this.metric = metric;
		return this;
	}
	public MetricDefinitionBuilder setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}
	public MetricDefinitionBuilder setDisplayNameShort(String displayNameShort) {
		this.displayNameShort = displayNameShort;
		return this;
	}
	public MetricDefinitionBuilder setDescription(String description) {
		this.description = description;
		return this;
	}
	public MetricDefinitionBuilder setDefaultResolutionMS(long defaultResolutionMS) {
		this.defaultResolutionMS = defaultResolutionMS;
		return this;
	}
	public MetricDefinitionBuilder setDefaultAggregate(MetricAggregates defaultAggregate) {
		this.defaultAggregate = defaultAggregate;
		return this;
	}
	public MetricDefinitionBuilder setUnit(MetricUnit unit) {
		this.unit = unit;
		return this;
	}
	
	public MetricDefinition build() {
		MetricDefinition m = new MetricDefinition();
		m.defaultAggregate = this.defaultAggregate;
		m.defaultResolutionMS = this.defaultResolutionMS;
		m.description = this.description;
		m.displayName = this.displayName;
		m.displayNameShort = this.displayNameShort;
		m.isDisabled = this.isDisabled;
		m.metric = this.metric;
		m.unit = this.unit;

		return m;
	}
}
