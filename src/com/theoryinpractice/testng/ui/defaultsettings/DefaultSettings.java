package com.theoryinpractice.testng.ui.defaultsettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.util.*;
import org.jdom.Element;

public class DefaultSettings implements JDOMExternalizable
{

    private String outputDirectory;
    private Map<String, String> defaultParameters = new HashMap<String, String>();

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Map getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(Map defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);

        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            List<Element> children = propertiesElement.getChildren("property");
            for (Element property : children) {
                defaultParameters.put(
                        property.getAttributeValue("name"),
                        property.getAttributeValue("value"));
            }
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {

        DefaultJDOMExternalizer.writeExternal(this, element);
        Element newElement = new Element("outputDirectory");
        newElement.setText(outputDirectory);
        element.addContent(newElement);

        Element propertiesElement = element.getChild("properties");
        if (propertiesElement == null) {
            propertiesElement = new Element("properties");
            element.addContent(propertiesElement);
        }

        for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
            Element property = new Element("property");
            property.setAttribute("name", entry.getKey());
            property.setAttribute("value", entry.getValue());
            propertiesElement.addContent(property);
        }


    }
}