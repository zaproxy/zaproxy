/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;
import org.zaproxy.zap.model.SessionStructure;

class XmlUtilsUnitTest {

    @Test
    void shouldReturnStrForEmptyXml() throws Exception {
        // given
        String xml = "<aaa/>";
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(result, is("<aaa>"));
    }

    @Test
    void shouldReturnStrForSimpleXml() throws Exception {
        // given
        String xml = "<aaa><bbb>BBB</bbb><ccc>CCC</ccc><ddd>DDD</ddd></aaa>";
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(result, is("<aaa:<bbb>,<ccc>,<ddd>>"));
    }

    @Test
    void shouldReturnStrForComplexElementXml() throws Exception {
        // given
        String xml =
                "<description>It happened on <date lang=\"norwegian\">03.03.99</date> etc</description> ";
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(result, is("<description:<date>>"));
    }

    @Test
    void shouldReturnStrForDeeperDuplicatedXml() throws Exception {
        // given
        String xml =
                """
                <breakfast_menu>
                    <food>
                        <name>Belgian Waffles</name>
                        <price>$5.95</price>
                        <description>
                        Two of our famous Belgian Waffles with plenty of real maple syrup
                        </description>
                        <calories>650</calories>
                    </food>
                    <food>
                        <name>Strawberry Belgian Waffles</name>
                        <price>$7.95</price>
                        <description>
                        Light Belgian waffles covered with strawberries and whipped cream
                        </description>
                        <calories>900</calories>
                    </food>
                    <food>
                        <name>Berry-Berry Belgian Waffles</name>
                        <price>$8.95</price>
                        <description>
                        Light Belgian waffles covered with an assortment of fresh berries and whipped cream
                        </description>
                        <calories>900</calories>
                    </food>
                    <food>
                        <name>French Toast</name>
                        <price>$4.50</price>
                        <description>
                        Thick slices made from our homemade sourdough bread
                        </description>
                        <calories>600</calories>
                    </food>
                    <food>
                        <name>Homestyle Breakfast</name>
                        <price>$6.95</price>
                        <description>
                        Two eggs, bacon or sausage, toast, and our ever-popular hash browns
                        </description>
                        <calories>950</calories>
                    </food>
                </breakfast_menu>
                """;
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(result, is("<breakfast_menu:<food:<name>,<price>,<description>,<calories>>..>"));
    }

    @Test
    void shouldReturnStrForEvenDeeperDuplicatedXml() throws Exception {
        // given
        // c/o
        // https://learn.microsoft.com/en-us/dotnet/standard/linq/sample-xml-file-multiple-purchase-orders
        String xml =
                """
                <?xml version="1.0"?>
                <PurchaseOrders>
                  <PurchaseOrder PurchaseOrderNumber="99503" OrderDate="1999-10-20">
                    <Address Type="Shipping">
                      <Name>Ellen Adams</Name>
                      <Street>123 Maple Street</Street>
                      <City>Mill Valley</City>
                      <State>CA</State>
                      <Zip>10999</Zip>
                      <Country>USA</Country>
                    </Address>
                    <Address Type="Billing">
                      <Name>Tai Yee</Name>
                      <Street>8 Oak Avenue</Street>
                      <City>Old Town</City>
                      <State>PA</State>
                      <Zip>95819</Zip>
                      <Country>USA</Country>
                    </Address>
                    <DeliveryNotes>Please leave packages in shed by driveway.</DeliveryNotes>
                    <Items>
                      <Item PartNumber="872-AA">
                        <ProductName>Lawnmower</ProductName>
                        <Quantity>1</Quantity>
                        <USPrice>148.95</USPrice>
                        <Comment>Confirm this is electric</Comment>
                      </Item>
                      <Item PartNumber="926-AA">
                        <ProductName>Baby Monitor</ProductName>
                        <Quantity>2</Quantity>
                        <USPrice>39.98</USPrice>
                        <ShipDate>1999-05-21</ShipDate>
                      </Item>
                    </Items>
                  </PurchaseOrder>
                  <PurchaseOrder PurchaseOrderNumber="99505" OrderDate="1999-10-22">
                    <Address Type="Shipping">
                      <Name>Cristian Osorio</Name>
                      <Street>456 Main Street</Street>
                      <City>Buffalo</City>
                      <State>NY</State>
                      <Zip>98112</Zip>
                      <Country>USA</Country>
                    </Address>
                    <Address Type="Billing">
                      <Name>Cristian Osorio</Name>
                      <Street>456 Main Street</Street>
                      <City>Buffalo</City>
                      <State>NY</State>
                      <Zip>98112</Zip>
                      <Country>USA</Country>
                    </Address>
                    <DeliveryNotes>Please notify me before shipping.</DeliveryNotes>
                    <Items>
                      <Item PartNumber="456-NM">
                        <ProductName>Power Supply</ProductName>
                        <Quantity>1</Quantity>
                        <USPrice>45.99</USPrice>
                      </Item>
                    </Items>
                  </PurchaseOrder>
                  <PurchaseOrder PurchaseOrderNumber="99504" OrderDate="1999-10-22">
                    <Address Type="Shipping">
                      <Name>Jessica Arnold</Name>
                      <Street>4055 Madison Ave</Street>
                      <City>Seattle</City>
                      <State>WA</State>
                      <Zip>98112</Zip>
                      <Country>USA</Country>
                    </Address>
                    <Address Type="Billing">
                      <Name>Jessica Arnold</Name>
                      <Street>4055 Madison Ave</Street>
                      <City>Buffalo</City>
                      <State>NY</State>
                      <Zip>98112</Zip>
                      <Country>USA</Country>
                    </Address>
                    <Items>
                      <Item PartNumber="898-AZ">
                        <ProductName>Computer Keyboard</ProductName>
                        <Quantity>1</Quantity>
                        <USPrice>29.99</USPrice>
                      </Item>
                      <Item PartNumber="898-AM">
                        <ProductName>Wireless Mouse</ProductName>
                        <Quantity>1</Quantity>
                        <USPrice>14.99</USPrice>
                      </Item>
                    </Items>
                  </PurchaseOrder>
                </PurchaseOrders>
                """;
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(
                result,
                is(
                        "<PurchaseOrders:<PurchaseOrder:<Address:<Name>,<Street>,<City>,<State>,<Zip>,<Country>>..,"
                                + "<DeliveryNotes>,<Items:"
                                + "<Item:<ProductName>,<Quantity>,<USPrice>,<Comment>>,"
                                + "<Item:<ProductName>,<Quantity>,<USPrice>,<ShipDate>>>>,"
                                + "<PurchaseOrder:<Address:<Name>,<Street>,<City>,<State>,<Zip>,<Country>>..,"
                                + "<DeliveryNotes>,<Items:"
                                + "<Item:<ProductName>,<Quantity>,<USPrice>>>>,"
                                + "<PurchaseOrder:<Address:<Name>,<Street>,<City>,<State>,<Zip>,<Country>>..,"
                                + "<Items:<Item:<ProductName>,<Quantity>,<USPrice>>..>>>"));
    }

    @Test
    void shouldReturnStrForMultipleMatchingChildNodesInXml() throws Exception {
        // given
        String xml =
                "<actions>"
                        + "<action>BBB<a>AA</a></action>"
                        + "<action>CCC<c>C1</c></action>"
                        + "<action>DDD<c>C2</c></action>"
                        + "<action>DDD<c>C3</c></action>"
                        + "<action>EEE<b>B1</b></action>"
                        + "<action>FFF<b>B2</b></action>"
                        + "</actions>";
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(result, is("<actions:<action:<a>>,<action:<c>>..,<action:<b>>..>"));
    }

    @Test
    void shouldReturnTrimmedStrForVeryLongXml() throws Exception {
        // given
        String cp =
                "<aaaaaaaaaa><bbbbbbbbbb></bbbbbbbbbb></aaaaaaaaaa>"
                        + "<bbbbbbbbbb><aaaaaaaaaa></aaaaaaaaaa></bbbbbbbbbb>";
        String xml = "<parent>" + String.join("", Collections.nCopies(10, cp)) + "</parent>";
        String expectedCp = "<aaaaaaaaaa:<bbbbbbbbbb>>,<bbbbbbbbbb:<aaaaaaaaaa>>,";
        String expectedXml = "<parent:" + String.join("", Collections.nCopies(10, expectedCp));
        // when
        String result = XmlUtils.getXmlKeyString(xml);
        // then
        assertThat(
                result,
                is(expectedXml.substring(0, SessionStructure.MAX_NODE_NAME_SIZE - 3) + "..."));
    }

    @Test
    void shouldThrowExceptionForInvalidXml() throws Exception {
        // given / when / then
        assertThrows(SAXParseException.class, () -> XmlUtils.getXmlKeyString("<aaa"));
    }
}
