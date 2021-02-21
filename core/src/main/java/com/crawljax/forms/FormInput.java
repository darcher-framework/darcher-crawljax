/*
 * Created Aug 14, 2008
 */
package com.crawljax.forms;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.google.common.base.Enums;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mesbah
 */
public class FormInput {

	// TODO troublor modify starts:
	@FunctionalInterface
	public interface InputGenerator {
		InputValue generate(WebDriver driver, WebElement webElement, Element nodeElement);
	}

	@FunctionalInterface
	public interface InputFiller {
		void fillInput(WebDriver driver, WebElement webElement, Element nodeElement);
	}
	// troublor modify ends

	public enum InputType {
		TEXT, RADIO, CHECKBOX, PASSWORD, HIDDEN, SELECT, TEXTAREA, NUMBER,
		// TODO troublor modify starts: add more InputTypes
		FILE,
		DYNAMIC, // dynamically generate input values, only support text-like input values for now
		CUSTOMIZE // the html element that simulate form inputs using other tags (e.g. div)
		// troublor modify ends
	}

	private InputType type = InputType.TEXT;

	private Identification identification;

	// TODO troublor modify starts: utility for added input types
	/**
     * Only used for {@link InputType#DYNAMIC}, to generate input value dynamically
     */
	private InputGenerator inputGenerator;

	/**
	 * Only used for {@link InputType#CUSTOMIZE}, to fill input dynamically
	 */
	private InputFiller inputFiller;
	// troublor modify ends

	private Set<InputValue> inputValues = new HashSet<>();
	private Eventable eventable;

	public FormInput(InputType type, Identification identification) {
		this.type = type;
		this.identification = identification;
	}

	/**
	 * @param type           the type of the input elements (e.g. text, checkbox)
	 * @param identification the identification.
	 * @param value          the value of the elements. 1 for checked
	 */
	public FormInput(InputType type, Identification identification, String value) {
		this.type = type;
		this.identification = identification;
		inputValues.add(new InputValue(value, value.equals("1")));
	}

	// TODO troublor modify starts: constructors for added input types
	/**
	 * Constructor for {@link InputType#DYNAMIC} form input
	 *
	 * @param identification the identification.
	 * @param inputGenerator the input generator object.
	 */
	public FormInput(Identification identification, InputGenerator inputGenerator) {
		this(InputType.DYNAMIC, identification);
		this.inputGenerator = inputGenerator;
	}

	/**
	 * 	 * Constructor for {@link InputType#CUSTOMIZE} form input
	 *
	 * @param identification the identification.
	 * @param inputFiller    the input filler object
	 */
	public FormInput(Identification identification, InputFiller inputFiller) {
		this(InputType.CUSTOMIZE, identification);
		this.inputFiller = inputFiller;
	}
	// troublor modify ends

	/**
	 * @return the input type.
	 */
	public InputType getType() {
		return type;
	}

	public static InputType getTypeFromStr(String type) {
		return Enums.getIfPresent(InputType.class, type.toUpperCase()).or(InputType.TEXT);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FormInput)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final FormInput rhs = (FormInput) obj;

		return new EqualsBuilder().append(this.identification, rhs.getIdentification())
				.append(this.type, rhs.getType()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.identification).append(this.type).toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	/**
	 * @param inputs         form input set.
	 * @param identification the identification to check.
	 * @return true if set contains a FormInput that has the same identification.
	 */
	public static boolean containsInput(Set<FormInput> inputs, Identification identification) {
		for (FormInput input : inputs) {
			if (input.getIdentification().equals(identification)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param inputs         form input set.
	 * @param identification the identification to check.
	 * @return a FormInput object that has the same identification.
	 */
	public static FormInput getInput(Set<FormInput> inputs, Identification identification) {
		for (FormInput input : inputs) {
			if (input.getIdentification().equals(identification)) {
				return input;
			}
		}

		return null;
	}

	// TODO troublor modify starts: getter/setter for added input types
	public InputGenerator getInputGenerator() {
        return inputGenerator;
    }

	public InputFiller getInputFiller() {
		return inputFiller;
	}

	public void setInputGenerator(InputGenerator inputGenerator) {
		this.inputGenerator = inputGenerator;
	}

	public void setInputFiller(InputFiller inputFiller) {
		this.inputFiller = inputFiller;
	}
	// troublor modify ends

	/**
	 * @return the inputValues
	 */
	public Set<InputValue> getInputValues() {
		return inputValues;
	}

	/**
	 * @param inputValues the inputValues to set
	 */
	public void inputValues(Set<InputValue> inputValues) {
		this.inputValues = inputValues;
	}

	public void inputValues(String... values) {
		for (String value : values) {
			InputValue inputValue = new InputValue(value);
			this.inputValues.add(inputValue);
		}
	}

	/**
	 * Sets the values of this input field. Only Applicable check-boxes and a radio buttons.
	 *
	 * @param values Values to set.
	 */
	public void inputValues(boolean... values) {
		for (boolean value : values) {
			InputValue inputValue = new InputValue();
			inputValue.setChecked(value);

			this.inputValues.add(inputValue);
		}
	}

	/**
	 * @return the related eventable for submitting
	 */
	public Eventable getEventable() {
		return eventable;
	}

	/**
	 * @param eventable the eventable by which this FormInput is submitted by
	 */
	public void setEventable(Eventable eventable) {
		this.eventable = eventable;
	}

	/**
	 * @return the identification
	 */
	public Identification getIdentification() {
		return identification;
	}

}
