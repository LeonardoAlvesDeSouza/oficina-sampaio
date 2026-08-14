/*
 * Confirmação de ações que desfazem alguma coisa.
 *
 * O envio é interceptado, não o clique: se este arquivo não carregar, ou o
 * navegador não tiver <dialog>, o formulário segue direto e a ação continua
 * funcionando — a confirmação é acréscimo, não requisito.
 */
(function () {
    'use strict';

    var SELETOR_FORMULARIO = 'form[data-confirmar]';

    function caixaDeConfirmacao() {
        var caixa = document.getElementById('confirmacao');
        return caixa && typeof caixa.showModal === 'function' ? caixa : null;
    }

    function preencher(caixa, formulario) {
        var alvo = caixa.querySelector('[data-confirmar-alvo]');
        if (alvo) {
            alvo.textContent = formulario.getAttribute('data-confirmar');
        }
    }

    document.addEventListener('submit', function (evento) {
        var formulario = evento.target.closest(SELETOR_FORMULARIO);
        if (!formulario || formulario.dataset.confirmado === 'sim') {
            return;
        }

        var caixa = caixaDeConfirmacao();
        if (!caixa) {
            return;
        }

        evento.preventDefault();
        preencher(caixa, formulario);
        caixa.returnValue = '';
        caixa.showModal();

        caixa.addEventListener('close', function aoFechar() {
            caixa.removeEventListener('close', aoFechar);
            if (caixa.returnValue !== 'confirmar') {
                return;
            }
            // Marca antes de reenviar: sem isto o próprio reenvio cairia neste
            // mesmo ouvinte e a caixa abriria de novo, para sempre.
            formulario.dataset.confirmado = 'sim';
            if (typeof formulario.requestSubmit === 'function') {
                formulario.requestSubmit();
            } else {
                formulario.submit();
            }
        });
    });
})();
