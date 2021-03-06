/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core.adapters;

import org.dcm4che3.conf.core.api.internal.ConfigTypeAdapter;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MapTypeAdapter<K, V> implements ConfigTypeAdapter<Map<K, V>, Map<Object, Object>> {


    @Override
    public Map<K, V> fromConfigNode(Map<Object, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        AnnotatedConfigurableProperty keyPseudoProperty = property.getPseudoPropertyForGenericsParamater(0);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyPseudoProperty);

        ConfigTypeAdapter<V, Object> valueAdapter;
        AnnotatedConfigurableProperty valuePseudoProperty = property.getPseudoPropertyForGenericsParamater(1);
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            valueAdapter = vitalizer.getReferenceTypeAdapter();
        else
            valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valuePseudoProperty);

        Map<K, V> map = new TreeMap<K, V>();

        for (Entry<Object, Object> e : configNode.entrySet()) {
            map.put(keyAdapter.fromConfigNode(keyAdapter.normalize(e.getKey(), keyPseudoProperty, vitalizer), keyPseudoProperty, vitalizer),
                    valueAdapter.fromConfigNode(valueAdapter.normalize(e.getValue(),valuePseudoProperty,vitalizer), valuePseudoProperty, vitalizer));

        }

        return map;
    }

    @Override
    public Map<Object, Object> toConfigNode(Map<K, V> object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        AnnotatedConfigurableProperty keyPseudoProperty = property.getPseudoPropertyForGenericsParamater(0);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyPseudoProperty);

        ConfigTypeAdapter<V, Object> valueAdapter;
        AnnotatedConfigurableProperty valuePseudoProperty = property.getPseudoPropertyForGenericsParamater(1);
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            valueAdapter = vitalizer.getReferenceTypeAdapter();
        else
            valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valuePseudoProperty);

        Map<Object, Object> configNode = new TreeMap<Object, Object>();

        for (Entry<K, V> e : object.entrySet()) {
            configNode.put(keyAdapter.toConfigNode(e.getKey(), keyPseudoProperty, vitalizer),
                    valueAdapter.toConfigNode(e.getValue(), valuePseudoProperty, vitalizer));
        }

        return configNode;
    }

    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        Map<String, Object> metadata = new HashMap<String, Object>();
        Map<String, Object> keyMetadata = new HashMap<String, Object>();
        Map<String, Object> valueMetadata = new HashMap<String, Object>();
        Map<String, Object> valueMetadataWrapper = new HashMap<String, Object>();

        metadata.put("type", "object");
        metadata.put("class", "Map");

        // get adapters
        AnnotatedConfigurableProperty keyPseudoProperty = property.getPseudoPropertyForGenericsParamater(0);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyPseudoProperty);

        AnnotatedConfigurableProperty valuePseudoProperty = property.getPseudoPropertyForGenericsParamater(1);
        ConfigTypeAdapter<V, Object> valueAdapter;
        if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
            valueAdapter = vitalizer.getReferenceTypeAdapter();
        else
            valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valuePseudoProperty);

        // fill in key and value metadata
        keyMetadata.putAll(keyAdapter.getSchema(keyPseudoProperty, vitalizer));
        metadata.put("mapkey", keyMetadata);

        valueMetadata.putAll(valueAdapter.getSchema(valuePseudoProperty, vitalizer));
        valueMetadataWrapper.put("*", valueMetadata);
        metadata.put("properties", valueMetadataWrapper);

        return metadata;
    }

    @Override
    public Map<Object, Object> normalize(Object configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        if (configNode == null) return new HashMap<Object, Object>();
        return (Map<Object, Object>) configNode;
    }
}