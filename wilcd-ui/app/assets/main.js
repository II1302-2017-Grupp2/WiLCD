import './main.less';

// import _ from 'underscore';
// import { jQuery, $ } from 'jquery';
import 'bootstrap/js/collapse'
import 'bootstrap-material-design/dist/js/material';
import 'bootstrap-material-design/dist/js/ripples';
import 'bootstrap-material-datetimepicker/js/bootstrap-material-datetimepicker';
import 'dropdown.js';

$.material.init();

// Style dropdowns
$('select.form-control').dropdown();

// Style date pickers
// We can't use type="datetime-local" since that doesn't let us style the field ourselves
$('input.input-datetime').each(function() {
    $(this).bootstrapMaterialDatePicker({
        format: 'YYYY-MM-DD HH:mm',
        clearButton: true,
        clearText: $(this).attr('placeholder') || "Clear"
    });
});

// We can't expand/collapse with JS disabled, so just keep them expanded in that case
// Here we disable those hacks when JS is on
$('.js-only').removeClass('js-only');
$('#navbar-collapse').removeClass('in');

// Guess user's timezone
const tzFormField = $('.form-signup input#timezone');
if (tzFormField.attr('value') === 'UTC (default)') {
    tzFormField.attr('value', moment.tz.guess());
}
