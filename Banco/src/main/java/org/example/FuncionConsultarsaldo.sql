-- FUNCTION: public.obtenersaldo(character varying)

-- DROP FUNCTION IF EXISTS public.obtenersaldo(character varying);

CREATE OR REPLACE FUNCTION public.obtenersaldo(
	id_cliente character varying DEFAULT '0000000A'::character varying,
	OUT cheles integer,
	OUT iban integer)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query select saldo, numero from cuentas where dni_titular = id_cliente;
end;
$BODY$;

ALTER FUNCTION public.obtenersaldo(character varying)
    OWNER TO postgres;
