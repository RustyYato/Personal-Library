package node;

import property.propertyset.PropertySet;

public class PropertiesNode<V> extends ValueNode<PropertySet<V>> {
	@SafeVarargs
	public PropertiesNode(Node... connections) {
		super(new PropertySet<>() , connections);
	}
	
	@Override
	public void setValue(PropertySet<V> value) {
		throw new UnsupportedOperationException("Cannot create new property set");
	}
}
