<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="html" encoding="utf-8" />

	<xsl:template match="/report">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		<html>
			<head>
				<meta http-equiv='Content-Type' content="text/html; charset=UTF-8" />
				<title>
					<xsl:value-of select="localization/title" />
				</title>
				<style>
					#table{width:100%;border:0;font-family:Arial,Helvetica,sans-serif;margin:
					10px 0px;}#table
					thead
					th{background-color:#6D929B;font-weight:700;color:#FFF;font-size:13px;padding:7px
					10px;-webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}#table
					tr{font-size:.8em;color:#446}#table
					tr:nth-of-type(odd){background:#D6EDEF}#table
					td{padding:5px}th.sort-header{cursor:pointer}th.sort-header::-moz-selection,th.sort-header::selection{background:0
					0}table
					th.sort-header:after{content:'';float:right;margin-top:7px;border-width:0
					4px 4px;border-style:solid;border-color:#fff
					transparent;visibility:hidden}table
					th.sort-header:hover:after{visibility:visible}table
					th.sort-down:after,table th.sort-down:hover:after,table
					th.sort-up:after{visibility:visible;opacity:.6}table
					th.sort-up:after{border-bottom:none;border-width:4px 4px 0}</style>
			</head>

			<body>
				<h1>
					<xsl:value-of select="localization/title" />
				</h1>
				<button onclick="showAll()">
					<xsl:value-of select="//localization/show-all" />
				</button>
				<button onclick="showIllegal()">
					<xsl:value-of select="//localization/show-illegal" />
				</button>
				<button onclick="showLegal()">
					<xsl:value-of select="//localization/show-valid" />
				</button>
				<input type="search" class="light-table-filter" data-table="rules-table"
					placeholder="Filter by text" />
				<table id="table" class="rules-table">
					<thead>
						<tr>
							<th>
							</th>
							<th>
							</th>
							<xsl:for-each select="users/user">
								<th colspan="2">
									<xsl:value-of select="@name" />
								</th>
							</xsl:for-each>
						</tr>
						<tr>
							<th>
								<xsl:value-of select="//localization/method" />
							</th>
							<th>
								<xsl:value-of select="//localization/url" />
							</th>
							<xsl:for-each select="users/user">
								<th>
									<xsl:value-of select="//localization/authorization" />
								</th>
								<th>
									<xsl:value-of select="//localization/access-control" />
								</th>
							</xsl:for-each>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each select="results/result">
							<tr>
								<td>
									<xsl:value-of select="@method" />
								</td>
								<td>
									<a href="{@uri}">
										<xsl:value-of select="@uri" />
									</a>
								</td>
								<xsl:for-each select="userResult">
									<td>
										<xsl:value-of select="@authorization" />
									</td>
									<td class="{@access-control}">
										<xsl:value-of select="@access-control-localized" />
									</td>
								</xsl:for-each>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
				<xsl:text disable-output-escaping='yes'>
				<script>
					function showAll(){var e=document.getElementById(&quot;table&quot;);for(var t=2;t&lt;e.rows.length;t++){e.rows[t].style.display=&quot;&quot;}}function showIllegal(){var e=document.getElementById(&quot;table&quot;);for(var t=2;t&lt;e.rows.length;t++){var n=e.rows[t];e.rows[t].style.display=&quot;none&quot;;for(var r=3;r&lt;n.cells.length;r+=2){if(n.cells[r].className===&quot;ILLEGAL&quot;)e.rows[t].style.display=&quot;&quot;}}}function showLegal(){var e=document.getElementById(&quot;table&quot;);for(var t=2;t&lt;e.rows.length;t++){var n=e.rows[t];e.rows[t].style.display=&quot;&quot;;for(var r=3;r&lt;n.cells.length;r+=2){if(n.cells[r].className===&quot;ILLEGAL&quot;)e.rows[t].style.display=&quot;none&quot;}}}(function(){function e(e,t){if(e.tagName!==&quot;TABLE&quot;){throw new Error(&quot;Element must be a table&quot;)}this.init(e,t||{})}e.prototype={init:function(e,t){var n=this,r;this.thead=false;this.options=t;this.options.d=t.descending||false;if(e.rows&amp;&amp;e.rows.length&gt;0){if(e.tHead&amp;&amp;e.tHead.rows.length&gt;0){r=e.tHead.rows[e.tHead.rows.length-1];n.thead=true}else{r=e.rows[0]}}if(!r){return}var i=function(e){var t=o(u,&quot;tr&quot;).getElementsByTagName(&quot;th&quot;);for(var r=0;r&lt;t.length;r++){if(c(t[r],&quot;sort-up&quot;)||c(t[r],&quot;sort-down&quot;)){if(t[r]!==this){t[r].className=t[r].className.replace(&quot; sort-down&quot;,&quot;&quot;).replace(&quot; sort-up&quot;,&quot;&quot;)}}}n.current=this;n.sortTable(this)};for(var s=0;s&lt;r.cells.length;s++){var u=r.cells[s];if(!c(u,&quot;no-sort&quot;)){u.className+=&quot; sort-header&quot;;h(u,&quot;click&quot;,i)}}},getFirstDataRowIndex:function(){if(!this.thead){return 1}else{return 0}},sortTable:function(e,t){var n=this,r=e.cellIndex,h,p=o(e,&quot;table&quot;),d=&quot;&quot;,v=n.getFirstDataRowIndex();if(p.rows.length&lt;=1)return;while(d===&quot;&quot;&amp;&amp;v&lt;p.tBodies[0].rows.length){d=u(p.tBodies[0].rows[v].cells[r]);d=f(d);if(d.substr(0,4)===&quot;&lt;!--&quot;||d.length===0){d=&quot;&quot;}v++}if(d===&quot;&quot;)return;var m=function(e,t){var r=u(e.cells[n.col]).toLowerCase(),i=u(t.cells[n.col]).toLowerCase();if(r===i)return 0;if(r&lt;i)return 1;return-1};var g=function(e,t){var r=u(e.cells[n.col]),i=u(t.cells[n.col]);r=l(r);i=l(i);return a(i,r)};var y=function(e,t){var r=u(e.cells[n.col]).toLowerCase(),i=u(t.cells[n.col]).toLowerCase();return s(i)-s(r)};if(d.match(/^-?[&#163;\x24&#219;&#162;&#180;€] ?\d/)||d.match(/^-?\d+\s*[€]/)||d.match(/^-?(\d+[,\.]?)+(E[\-+][\d]+)?%?$/)){h=g}else if(i(d)){h=y}else{h=m}this.col=r;var b=[],w={},E,S=0;for(v=0;v&lt;p.tBodies.length;v++){for(E=0;E&lt;p.tBodies[v].rows.length;E++){var x=p.tBodies[v].rows[E];if(c(x,&quot;no-sort&quot;)){w[S]=x}else{b.push({tr:x,index:S})}S++}}if(!t){if(n.options.d){if(c(e,&quot;sort-up&quot;)){e.className=e.className.replace(/ sort-up/,&quot;&quot;);e.className+=&quot; sort-down&quot;}else{e.className=e.className.replace(/ sort-down/,&quot;&quot;);e.className+=&quot; sort-up&quot;}}else{if(c(e,&quot;sort-down&quot;)){e.className=e.className.replace(/ sort-down/,&quot;&quot;);e.className+=&quot; sort-up&quot;}else{e.className=e.className.replace(/ sort-up/,&quot;&quot;);e.className+=&quot; sort-down&quot;}}}var T=function(e){return function(t,n){var r=e(t.tr,n.tr);if(r===0){return t.index-n.index}return r}};var N=function(e){return function(t,n){var r=e(t.tr,n.tr);if(r===0){return n.index-t.index}return r}};if(c(e,&quot;sort-down&quot;)){b.sort(N(h));b.reverse()}else{b.sort(T(h))}var C=0;for(v=0;v&lt;S;v++){var k;if(w[v]){k=w[v];C++}else{k=b[v-C].tr}p.tBodies[0].appendChild(k)}},refresh:function(){if(this.current!==undefined){this.sortTable(this.current,true)}}};var t=/(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\.?\,?\s*/i,n=/\d{1,2}[\/\-]\d{1,2}[\/\-]\d{2,4}/,r=/(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/i;var i=function(e){return(e.search(t)!==-1||e.search(n)!==-1||e.search(r!==-1))!==-1&amp;&amp;!isNaN(s(e))},s=function(e){e=e.replace(/\-/g,&quot;/&quot;);e=e.replace(/(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{2})/,&quot;$1/$2/$3&quot;);return(new Date(e)).getTime()},o=function(e,t){if(e===null){return null}else if(e.nodeType===1&amp;&amp;e.tagName.toLowerCase()===t.toLowerCase()){return e}else{return o(e.parentNode,t)}},u=function(e){var t=this;if(typeof e===&quot;string&quot;||typeof e===&quot;undefined&quot;){return e}var n=e.getAttribute(&quot;data-sort&quot;)||&quot;&quot;;if(n){return n}else if(e.textContent){return e.textContent}else if(e.innerText){return e.innerText}var r=e.childNodes,i=r.length;for(var s=0;s&lt;i;s++){switch(r[s].nodeType){case 1:n+=t.getInnerText(r[s]);break;case 3:n+=r[s].nodeValue;break}}return n},a=function(e,t){var n=parseFloat(e),r=parseFloat(t);e=isNaN(n)?0:n;t=isNaN(r)?0:r;return e-t},f=function(e){return e.replace(/^\s+|\s+$/g,&quot;&quot;)},l=function(e){return e.replace(/[^\-?0-9.]/g,&quot;&quot;)},c=function(e,t){return(&quot; &quot;+e.className+&quot; &quot;).indexOf(&quot; &quot;+t+&quot; &quot;)&gt;-1},h=function(e,t,n){if(e.attachEvent){e[&quot;e&quot;+t+n]=n;e[t+n]=function(){e[&quot;e&quot;+t+n](window.event)};e.attachEvent(&quot;on&quot;+t,e[t+n])}else{e.addEventListener(t,n,false)}};if(typeof module!==&quot;undefined&quot;&amp;&amp;module.exports){module.exports=e}else{window.Tablesort=e}})();new Tablesort(document.getElementById(&quot;table&quot;));(function(e){&quot;use strict&quot;;var t=function(t){function r(r){n=r.target;var s=e.getElementsByClassName(n.getAttribute(&quot;data-table&quot;));t.forEach.call(s,function(e){t.forEach.call(e.tBodies,function(e){t.forEach.call(e.rows,i)})})}function i(e){var t=e.textContent.toLowerCase(),r=n.value.toLowerCase();e.style.display=t.indexOf(r)===-1?&quot;none&quot;:&quot;table-row&quot;}var n;return{init:function(){var n=e.getElementsByClassName(&quot;light-table-filter&quot;);t.forEach.call(n,function(e){e.oninput=r})}}}(Array.prototype);e.addEventListener(&quot;readystatechange&quot;,function(){if(e.readyState===&quot;complete&quot;){t.init()}})})(document);
				</script>
				</xsl:text>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>