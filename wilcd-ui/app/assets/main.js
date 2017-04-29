import './main.less';

// import _ from 'underscore';
// import { jQuery, $ } from 'jquery';
import 'bootstrap-material-design/dist/js/material';
import 'bootstrap-material-datetimepicker/js/bootstrap-material-datetimepicker';
import 'dropdown.js';

$.material.init();
$('input[type=datetime]').bootstrapMaterialDatePicker({ format : 'DD MMMM YYYY - HH:mm' });
$('select').dropdown();
