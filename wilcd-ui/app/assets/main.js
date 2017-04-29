// import MaterialDateTimePicker from 'material-datetime-picker';
import './main.less';
// import _ from 'underscore';
// import { jQuery, $ } from 'jquery';
import 'bootstrap-material-design/dist/js/material';
import 'bootstrap-material-datetimepicker/js/bootstrap-material-datetimepicker';

$.material.init();
// _.each(document.querySelectorAll("input[type=datetime]"), (input) => {
//     const picker = new MaterialDateTimePicker()
//         .on('submit', (val) => {
//             input.value = val.format("DD/MM/YYYY");
//         });
//     input.addEventListener('focus', () => picker.open());
// });
$('input[type=datetime]').bootstrapMaterialDatePicker({ format : 'DD MMMM YYYY - HH:mm' });
