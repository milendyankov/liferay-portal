(function() {
	AUI().ready('liferay-sign-in-modal', function(A) {
		var signIn = A.one('.sign-in > a');

		if (signIn && signIn.getData('redirect') !== 'true') {
			signIn.plug(Liferay.SignInModal);
		}
	});

	Liferay.Loader.require('porygon-theme/js/top_search.es', function(
		TopSearch
	) {
		new TopSearch.default();
	});
})();
