import './main.less';

// import _ from 'underscore';
// import { jQuery, $ } from 'jquery';
import 'bootstrap/js/collapse'
import 'bootstrap-material-design/dist/js/material';
import 'bootstrap-material-design/dist/js/ripples';
import 'bootstrap-material-datetimepicker/js/bootstrap-material-datetimepicker';
import 'dropdown.js';

$.material.init();
$('input[type=datetime]').bootstrapMaterialDatePicker({ format : 'DD MMMM YYYY - HH:mm' });
$('select').dropdown();

// We can't expand/collapse with JS disabled, so just keep them expanded in that case
// Here we disable those hacks when JS is on
$('.js-only').removeClass('js-only');
$('#navbar-collapse').removeClass('in');
