(ColName integer, int_start integer, int_stop integer)
RETURNS integer AS $$
BEGIN
    RETURN (SELECT CAST ( random()*(int_stop-int_start)+int_start AS integer ));
END
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;