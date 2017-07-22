import csv
import sqlite3


def main():
    #for my own convenience/sanity - imports card data from a csv and adds it to the database
    #to prevent typos, it does not insert any new values in CardAttribute, CardClass, Requirement, or ThunderstoneSet
    csv_path = 'card_data.csv'
    db_path = '../assets/databases/cards.sqlite'

    db = sqlite3.connect(db_path)
    cursor = db.cursor()
    static_map = build_static_maps(cursor)

    card_insert_functions = {
        'Dungeon': insert_dungeon_card,
        'DungeonBoss': insert_dungeon_boss_card,
        'Hero': insert_hero_card,
        'Village': insert_village_card,
    }

    with open(csv_path) as csv_file:
        csv_reader = csv.reader(csv_file)
        header_row = csv_reader.next()
        for row_num, row in enumerate(csv_reader):
            card_data = {}
            for i, value in enumerate(row):
                header = header_row[i]
                card_data[header] = value
            category = card_data['Category']
            if category in card_insert_functions:
                convert_names_to_ids(card_data, static_map)
                insert_function = card_insert_functions[category]
                insert_function(card_data, cursor)
            else:
                print "Unsupported category '%s' for row %s" % (category, row_num+1)

    cursor.close()
    db.commit()
    db.close()
    print 'Done.'


def insert_dungeon_card(card_data, cursor):
    print 'Inserting Dungeon card: %s' % card_data
    unique_parameters = {
        'cardType': card_data['CardType (D,DB)'],
        'level': card_data['Level (D)'] or None,
    }
    insert_card('DungeonCard', card_data, unique_parameters, cursor)


def insert_dungeon_boss_card(card_data, cursor):
    print 'Inserting DungeonBoss card: %s' % card_data
    unique_parameters = {
        'cardType': card_data['CardType (D,DB)'],
    }
    insert_card('DungeonBossCard', card_data, unique_parameters, cursor)


def insert_hero_card(card_data, cursor):
    print 'Inserting Hero card: %s' % card_data
    unique_parameters = {
        'race': card_data['Race (H)'],
        'strength': card_data['Strength (H)'],
    }
    insert_card('HeroCard', card_data, unique_parameters, cursor)


def insert_village_card(card_data, cursor):
    print 'Inserting Village card: %s' % card_data
    unique_parameters = {
        'goldValue': card_data['Gold Value (V)'] or None,
        'goldCost': card_data['Gold Cost (V)'],
        'weight': card_data['Weight (V)'] or None,
    }
    insert_card('VillageCard', card_data, unique_parameters, cursor)


def insert_card(card_table_name, card_data, unique_parameters, cursor):
    card_name = card_data['Card Name']
    unique_parameters['cardName'] = card_name
    unique_parameters['description'] = card_data['Description']
    parameter_name_string = ', '.join(unique_parameters.keys())
    parameter_string = ', '.join( ('?',)*len(unique_parameters) )
    insert_sql = 'INSERT INTO %s (%s) values (%s)' % (card_table_name, parameter_name_string, parameter_string)
    cursor.execute(insert_sql, unique_parameters.values())
    select_sql = 'SELECT _ID FROM %s where cardName = ?' % card_table_name
    cursor.execute(select_sql, (card_name,))
    card_id = cursor.fetchone()[0]
    insert_relationships(card_table_name, card_id, card_data, cursor)

def insert_relationships(card_table_name, card_id, card_data, cursor):
    schema_info = {
        'Attributes': ('Card_CardAttribute', 'attributeId'),
        'Classes': ('Card_CardClass', 'classId'),
        'Requirements': ('Card_Requirement', 'requirementId'),
        'Set Name': ('Card_ThunderstoneSet', 'setId'),
    }
    for field_name, (table_name, id_field_name) in schema_info.iteritems():
        if card_data[field_name]:
            sql = 'INSERT INTO %s (cardTableName, cardId, %s) values (?, ?, ?)' % (table_name, id_field_name)
            param_list = [(card_table_name, card_id, id_value) for id_value in card_data[field_name]]
            cursor.executemany(sql, param_list)


def build_static_maps(cursor):
    static_map = {
        'Attributes': fetch_values('attributeName', 'CardAttribute', cursor),
        'Classes': fetch_values('className', 'CardClass', cursor),
        'Requirements': fetch_values('requirementName', 'Requirement', cursor),
        'Set Name': fetch_values('setName', 'ThunderstoneSet', cursor)
    }

    return static_map


def fetch_values(name_column, table_name, cursor):
    value_map = {}
    cursor.execute('SELECT _ID, %s from %s' % (name_column, table_name))
    results = cursor.fetchall()
    for value_id, name in results:
        value_map[name] = value_id
    return value_map


def convert_names_to_ids(card_data, static_map):
    for field_name in static_map:
        names = card_data.get(field_name)
        names = names.split(',')
        ids = [static_map[field_name][name] for name in names if name]
        card_data[field_name] = ids


if __name__ == '__main__':
    main()