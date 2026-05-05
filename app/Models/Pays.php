<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Pays extends Model
{
    protected $table = 'Pays';
    protected $primaryKey = 'idpays';
    public $timestamps = false;
    protected $guarded = [];
}
