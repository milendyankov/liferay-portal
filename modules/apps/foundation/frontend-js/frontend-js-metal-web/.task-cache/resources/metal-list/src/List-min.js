define("frontend-js-metal-web@1.0.8/metal-list/src/List-min", ["exports","metal-dom/src/all/dom","metal-component/src/all/component","metal-soy/src/Soy","./List.soy","metal-jquery-adapter/src/JQueryAdapter","./ListItem"], function(e,t,r,n,o,i){"use strict";function a(e){return e&&e.__esModule?e:{"default":e}}function l(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}function u(e,t){if(!e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!t||"object"!=typeof t&&"function"!=typeof t?e:t}function s(e,t){if("function"!=typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function, not "+typeof t);e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}),t&&(Object.setPrototypeOf?Object.setPrototypeOf(e,t):e.__proto__=t)}Object.defineProperty(e,"__esModule",{value:!0});var c=a(t),f=a(r),p=a(n),d=a(o),y=a(i),m=function(e){function t(){return (l(this,t), u(this,e.apply(this,arguments)))}return (s(t,e), t.prototype.handleClick=function(e){for(var t=e.target;t&&!c["default"].match(t,".listitem");)t=t.parentNode;this.emit("itemSelected",t)}, t)}(f["default"]);p["default"].register(m,d["default"]),m.STATE={items:{validator:Array.isArray,valueFn:function(){return[]}},itemsHtml:{isHtml:!0}},e["default"]=m,y["default"].register("list",m)});