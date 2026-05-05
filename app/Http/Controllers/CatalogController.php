<?php

namespace App\Http\Controllers;

use App\Models\Article;
use App\Models\Pays;
use App\Models\Role;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;

class CatalogController extends Controller
{
    public function roles()
    {
        return response()->json(Role::all());
    }

    public function createRole(Request $request)
    {
        $role = Role::create($request->validate(['librole' => ['required', 'string'], 'descrole' => ['nullable', 'string']]));
        return response()->json($role, 201);
    }

    public function updateRole(Request $request, int $idrole)
    {
        $role = Role::findOrFail($idrole);
        $role->update($request->only(['librole', 'descrole']));
        return response()->json($role);
    }

    public function deleteRole(int $idrole)
    {
        Role::where('idrole', $idrole)->delete();
        return response()->json(['deleted' => true]);
    }

    public function pays()
    {
        return response()->json(Pays::all());
    }

    public function createPays(Request $request)
    {
        $pays = Pays::create($request->validate(['libpays' => ['required', 'string'], 'descpays' => ['nullable', 'string']]));
        return response()->json($pays, 201);
    }

    public function updatePays(Request $request, int $idpays)
    {
        $pays = Pays::findOrFail($idpays);
        $pays->update($request->only(['libpays', 'descpays']));
        return response()->json($pays);
    }

    public function deletePays(int $idpays)
    {
        Pays::where('idpays', $idpays)->delete();
        return response()->json(['deleted' => true]);
    }

    public function familleArticles()
    {
        return response()->json(DB::table('FamilleArticle')->get());
    }

    public function createFamille(Request $request)
    {
        DB::table('FamilleArticle')->insert($request->validate([
            'libfamille' => ['required', 'string'],
            'description' => ['nullable', 'string'],
        ]));
        return response()->json(['created' => true], 201);
    }

    public function updateFamille(Request $request, int $idfamille)
    {
        DB::table('FamilleArticle')->where('idfamille', $idfamille)->update($request->only(['libfamille', 'description']));
        return response()->json(['updated' => true]);
    }

    public function deleteFamille(int $idfamille)
    {
        DB::table('FamilleArticle')->where('idfamille', $idfamille)->delete();
        return response()->json(['deleted' => true]);
    }

    public function typeArticles()
    {
        return response()->json(DB::table('TypeArticle')->get());
    }

    public function createType(Request $request)
    {
        DB::table('TypeArticle')->insert($request->validate([
            'libtype' => ['required', 'string'],
            'desctype' => ['nullable', 'string'],
            'idfamille' => ['nullable', 'integer'],
        ]));
        return response()->json(['created' => true], 201);
    }

    public function updateType(Request $request, int $idtype)
    {
        DB::table('TypeArticle')->where('idtype', $idtype)->update($request->only(['libtype', 'desctype', 'idfamille']));
        return response()->json(['updated' => true]);
    }

    public function deleteType(int $idtype)
    {
        DB::table('TypeArticle')->where('idtype', $idtype)->delete();
        return response()->json(['deleted' => true]);
    }

    public function articles(Request $request)
    {
        $query = Article::query();
        if ($request->boolean('international')) {
            $query->where('vendeurInternational', true);
        }
        return response()->json($query->get());
    }

    public function articleById(int $id)
    {
        return response()->json(Article::findOrFail($id));
    }

    public function createArticle(Request $request)
    {
        $article = Article::create($request->all());
        return response()->json($article, 201);
    }

    public function updateArticleJson(Request $request, int $id)
    {
        $article = Article::findOrFail($id);
        $article->update($request->all());
        return response()->json($article);
    }

    public function deleteArticle(int $id)
    {
        Article::where('idarticle', $id)->delete();
        return response()->json(['deleted' => true]);
    }

    public function adminArticles()
    {
        return response()->json(Article::orderByDesc('idarticle')->limit(200)->get());
    }

    public function adminPatchArticle(Request $request, int $id)
    {
        $article = Article::findOrFail($id);
        $article->update($request->all());
        return response()->json($article);
    }

    public function articlePhoto(int $id)
    {
        $article = Article::findOrFail($id);
        $filename = $article->photo ?? null;
        if (! $filename) {
            abort(404, 'Photo not found');
        }
        return $this->articlePhotoFile($id, $filename);
    }

    public function articlePhotoFile(int $id, string $filename)
    {
        $path = "articles/{$id}/{$filename}";
        if (! Storage::disk('public')->exists($path)) {
            abort(404, 'Photo not found');
        }
        return Storage::disk('public')->response($path);
    }
}
