drop table if exists public.game;
drop table if exists public.player;

create table game(id varchar(36) not null primary key, player_id varchar(36) not null,  deck integer[], top_card_index integer, table_tokens integer);
create table player(id varchar(36) not null primary key, player_name varchar(24) not null, token_count integer, unique(player_name));
