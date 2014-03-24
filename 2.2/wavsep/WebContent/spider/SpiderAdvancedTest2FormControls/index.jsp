<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<%@include file="/spider/must-visit-page.jsp"%>
<head>
<title>Spider Advanced Test</title>
</head>
<body>

	<h2>Test 2</h2>

	<p>Pages in this section should by used for testing crawlers and
		spiders. The spider should explore the links that are provided below.</p>

	<p>This test is for pages containing an HTML form with all control element types.</p>
	<br />
	<form action="a.jsp" method="get">
			<div class="text">
				<label for="type-text">Text (text)</label>
				<input type="text" name="type-text" id="type-text" value="text">
			</div>
			<div class="text">
				<label for="type-search">Search (search)</label>
				<input type="search" name="type-search" id="type-search">
			</div>
			<div class="text">
				<label for="type-tel">Telephone (tel)</label>
				<input type="tel" name="type-tel" id="type-tel">
			</div>
			<div class="text">
				<label for="type-url">URL (url)</label>
				<input type="url" name="type-url" id="type-url">
			</div>
			<div class="text">
				<label for="type-email">E-mail (email)</label>
				<input type="email" name="type-email" id="type-email">
			</div>
			<div class="text">
				<label for="type-datetime">Date and Time (datetime)</label>
				<input type="datetime" name="type-datetime" id="type-datetime">
			</div>
			<div class="text">
				<label for="type-date">Date (date)</label>
				<input type="date" name="type-date" id="type-date">
			</div>
			<div class="text">
				<label for="type-month">Month (month)</label>
				<input type="month" name="type-month" id="type-month">
			</div>
			<div class="text">
				<label for="type-week">Week (week)</label>
				<input type="week" name="type-week" id="type-week">
			</div>
			<div class="text">
				<label for="type-time">Time (time)</label>
				<input type="time" name="type-time" id="type-time">
			</div>
			<div class="text">
				<label for="type-datetime-local">Local Date and Time (datetime-local)</label>
				<input type="datetime-local" name="type-datetime-local" id="type-datetime-local">
			</div>
			<div class="text">
				<label for="type-number">Number (number)</label>
				<input type="number" name="type-number" id="type-number" min="0" max="20">
			</div>
			<div class="text">
				<label for="type-range">Range (range)</label>
				<input type="range" name="type-range" id="type-range" min="0" max="100">
			</div>
			<div class="text">
				<label for="type-color">Colour (color)</label>
				<input type="color" name="type-color" id="type-color">
			</div>
			<div class="multiple">
				<label for="type-checkbox1">Checkbox: </label>
				<input type="checkbox" name="type-checkbox" id="type-checkbox1" value="Value1"/>
				<input type="checkbox" name="type-checkbox" id="type-checkbox2" value="Value2"/>
			</div>
			
			<div class="multiple">
				<label for="type-checkbox2">Preselected Checkbox: </label>
				<input type="checkbox" name="type-checkbox2" id="type-checkbox3" value="Value1" checked="checked"/>
				<input type="checkbox" name="type-checkbox2" id="type-checkbox4" value="Value2" checked="checked"/>
			</div>
			
			<div class="multiple">
				<label for="type-radio1">Radio: </label>
				<input type="radio" name="type-radio" id="type-radio1" value="Value1" checked="checked"/>
				<input type="radio" name="type-radio" id="type-radio2" value="Value2"/>
			</div>
			
			<div class="multiple">
				<label for="type-select">Select: </label>
				<select name="type-select">
					<option value="type-select-value1">Value1</option>
					<option value="type-select-value2">Value2</option>
					<option value="type-select-value3" selected="selected">Value3</option>
					<option value="type-select-value4">Value4</option>
				</select> 
			</div>			 
			
			<div class="text">
				<label for="type-textarea">Text Area Control</label>
				<textarea name="type-textarea" rows=3 cols="25">Empty text area</textarea>
			</div>

			<input type="file" name="type-file" accept=".txt .doc .pdf">
			
			<div class="submit-area">
				<input type="submit" value="Submit" name="submit-1">
			</div>
	</form>



</body>
