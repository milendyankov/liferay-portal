import Component from 'metal-component';
import {isFunction, isObject} from 'metal';
import Soy from 'metal-soy';
import {Config} from 'metal-state';

import templates from './ContextualSidebar.soy';

/**
 * ContextualSidebar
 * @review
 */
class ContextualSidebar extends Component {
	/**
	 * @inheritDoc
	 * @review
	 */
	created() {
		document.body.classList.add('has-contextual-sidebar');

		this._productMenuToggle = $('.product-menu-toggle');
		this._productMenu = $(this._productMenuToggle.data('target'));

		this._handleOpenProductMenu = this._handleOpenProductMenu.bind(this);

		this._productMenu.on(
			'openStart.lexicon.sidenav',
			this._handleOpenProductMenu
		);
	}

	/**
	 * @inheritDoc
	 * @review
	 */
	disposed() {
		document.body.classList.remove('has-contextual-sidebar');

		this._productMenu.off(
			'openStart.lexicon.sidenav',
			this._handleOpenProductMenu
		);
	}

	/**
	 * @inheritDoc
	 * @review
	 */
	prepareStateForRender(state) {
		return Object.assign({}, state, {
			body: Soy.toIncDom(this.body || ''),
			header: Soy.toIncDom(this.header || ''),
		});
	}

	/**
	 * Disallow setting element display to none
	 * @inheritDoc
	 * @review
	 */
	syncVisible() {}

	/**
	 * @inheritDoc
	 * @review
	 */
	rendered() {
		if (this.visible) {
			document.body.classList.add('contextual-sidebar-visible');

			this._productMenuToggle.sideNavigation('hide');
		}
		else {
			document.body.classList.remove('contextual-sidebar-visible');
		}
	}

	/**
	 * Handle product menu's show event and emits a hide event.
	 * Components using ContextualSidebar should change it's visible
	 * property at this point.
	 * @private
	 * @review
	 */
	_handleOpenProductMenu() {
		this.emit('hide');
	}
}

/**
 * State definition.
 * @review
 * @static
 * @type {!Object}
 */
ContextualSidebar.STATE = {
	/**
	 * Sidebar body content
	 * @default undefined
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {!string}
	 */
	body: Config.any()
		.setter(body =>
			isObject(body) && !isFunction(body)
				? body.value.content
				: body
		)
		.required(),

	/**
	 * Optional CSS classes added to the sidebar body wrapper
	 * @default ''
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {string}
	 */
	bodyClasses: Config.string().value(''),

	/**
	 * Optional CSS classes added to the sidebar wrapper
	 * @default ''
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {string}
	 */
	classes: Config.string().value(''),

	/**
	 * Sidebar header content
	 * @default undefined
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {!string}
	 */
	header: Config.any()
		.setter(header =>
			isObject(header) && !isFunction(header)
				? header.value.content
				: header
		)
		.required(),

	/**
	 * Optional CSS classes added to the sidebar header wrapper
	 * @default ''
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {string}
	 */
	headerClasses: Config.string().value(''),

	/**
	 * Autogenerated id provided by the template engine
	 * @default ''
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {string}
	 */
	id: Config.string().value(''),

	/**
	 * Allow opening/closing contextual sidebar
	 * @default undefined
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {!boolean}
	 */
	visible: Config.bool().required(),

	/**
	 * Internal property that keeps an existing ProductMenu sidebar synced.
	 * @default undefined
	 * @instance
	 * @memberOf ContextualSidebar
	 * @review
	 * @type {object}
	 */
	_productMenu: Config.internal(),
};

Soy.register(ContextualSidebar, templates);

export {ContextualSidebar};
export default ContextualSidebar;
